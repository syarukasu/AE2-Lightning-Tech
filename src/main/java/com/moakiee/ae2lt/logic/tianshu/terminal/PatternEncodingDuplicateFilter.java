package com.moakiee.ae2lt.logic.tianshu.terminal;

import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.GenericStack;
import com.moakiee.ae2lt.item.ClosedLoopPatternItem;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopMemberPattern;
import com.moakiee.ae2lt.logic.tianshu.loop.ClosedLoopPatternPayload;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tianshuパターン保管枠へ同一パターンを重複登録しないための比較ヘルパーです。
 *
 * <p>通常パターンはアイテム状態、AE2デコード結果の順に比較し、閉ループパターンは
 * NBT payloadを比較します。デコードできない既存スタックは登録を妨げず、診断情報だけを残します。</p>
 */
public final class PatternEncodingDuplicateFilter {
    private static final Logger LOG = LoggerFactory.getLogger("ae2lt/TianshuDuplicate");
    private static final int NO_MATCHED_SLOT = -1;
    // ログのハッシュは短く読みやすい16進数で固定します。
    private static final int HASH_RADIX = 16;

    private PatternEncodingDuplicateFilter() {
    }

    public static boolean containsEquivalentPattern(
            InternalInventory inventory, ItemStack candidate, Level level) {
        return checkEquivalentPattern(inventory, candidate, level).duplicate();
    }

    public static CheckResult checkEquivalentPattern(
            InternalInventory inventory, ItemStack candidate, Level level) {
        // 空の候補や対象インベントリがない場合は、重複なしとして処理を続行します。
        if (inventory == null || candidate == null || candidate.isEmpty()) {
            return new CheckResult(false, NO_MATCHED_SLOT, MatchMethod.NONE, 0, 0);
        }

        ClosedLoopPatternPayload candidatePayload = readClosedLoopPayload(candidate);
        boolean candidateIsClosedLoop = candidatePayload != null;
        IPatternDetails candidateDetails = candidateIsClosedLoop
                ? null
                : decode(candidate, level);
        int occupiedSlots = 0;
        int undecodableSlots = 0;

        // 対象インベントリの全スロットを一度だけ走査して重複を判定します。
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            // 空スロットは比較対象ではないため、次のスロットへ進みます。
            if (stored.isEmpty()) {
                continue;
            }
            occupiedSlots++;

            // 閉ループ同士は展開後のAE2定義ではなく、保存されたpayloadを比較します。
            if (candidateIsClosedLoop) {
                ClosedLoopPatternPayload storedPayload = readClosedLoopPayload(stored);
                // 同一payloadの既存スロットが見つかったら重複として返します。
                if (sameClosedLoopPayload(storedPayload, candidatePayload)) {
                    return new CheckResult(true, slot, MatchMethod.CLOSED_LOOP_PAYLOAD,
                            occupiedSlots, undecodableSlots);
                }
                continue;
            }

            // 完全に同じItemStackなら、重いAE2デコードを行わずに判定できます。
            if (ItemStack.isSameItemSameTags(stored, candidate)) {
                return new CheckResult(true, slot, MatchMethod.EXACT_STACK,
                        occupiedSlots, undecodableSlots);
            }

            IPatternDetails storedDetails = decode(stored, level);
            // 壊れた既存パターンは統計だけ増やし、正常な登録候補を拒否しません。
            if (storedDetails == null) {
                undecodableSlots++;
            }
            // 定義が同じ、またはAE2の詳細比較が同じなら論理的な重複です。
            if (sameDetails(storedDetails, candidateDetails, stored, candidate)) {
                return new CheckResult(true, slot, MatchMethod.DECODED_DEFINITION,
                        occupiedSlots, undecodableSlots);
            }
        }

        // 通常パターンを候補側でデコードできなかった場合だけ診断ログを出します。
        if (!candidateIsClosedLoop && candidateDetails == null) {
            LOG.warn("Candidate could not be decoded during duplicate check: {}", describeStack(candidate));
        }
        return new CheckResult(false, NO_MATCHED_SLOT, MatchMethod.NONE,
                occupiedSlots, undecodableSlots);
    }

    private static ClosedLoopPatternPayload readClosedLoopPayload(ItemStack stack) {
        // 閉ループ用アイテム以外はpayloadを持たないため、通常パターンとして扱います。
        if (stack == null || !(stack.getItem() instanceof ClosedLoopPatternItem item)) {
            return null;
        }
        return item.readPayload(stack).orElse(null);
    }

    static boolean sameClosedLoopPayload(
            ClosedLoopPatternPayload first, ClosedLoopPatternPayload second) {
        // 片方だけが閉ループpayloadを持つ場合は同一とはみなしません。
        if (first == null || second == null) {
            return false;
        }
        return sameMembers(first.members(), second.members())
                && sameStacks(first.externalInputs(), second.externalInputs())
                && sameStacks(first.outputs(), second.outputs())
                && first.enabled() == second.enabled();
    }

    private static boolean sameMembers(
            List<ClosedLoopMemberPattern> first, List<ClosedLoopMemberPattern> second) {
        // メンバー数が違うpayloadは同一ではありません。
        if (first.size() != second.size()) {
            return false;
        }
        // メンバー順、コピー数、保存済み元パターンを順番どおり比較します。
        for (int index = 0; index < first.size(); index++) {
            ClosedLoopMemberPattern left = first.get(index);
            ClosedLoopMemberPattern right = second.get(index);
            // 同じ元パターンと同じ実行回数であることを確認します。
            if (left.copiesPerCycle() != right.copiesPerCycle()
                    || !Objects.equals(left.pattern().toTag(), right.pattern().toTag())) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameStacks(List<GenericStack> first, List<GenericStack> second) {
        // 入出力リストの長さが違うpayloadは同一ではありません。
        if (first.size() != second.size()) {
            return false;
        }
        // AEKeyと数量を順番どおり比較し、同じ収支だけを重複と判定します。
        for (int index = 0; index < first.size(); index++) {
            GenericStack left = first.get(index);
            GenericStack right = second.get(index);
            // 数量かAEKeyが異なる場合は別レシピです。
            if (left.amount() != right.amount() || !Objects.equals(left.what(), right.what())) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameDetails(
            IPatternDetails stored, IPatternDetails candidate,
            ItemStack storedStack, ItemStack candidateStack) {
        // どちらかをデコードできない場合は、推測で重複扱いにしません。
        if (stored == null || candidate == null) {
            return false;
        }
        try {
            // AE2の定義キーが一致すれば、NBTの表現差をまたいで同じレシピと判定できます。
            if (Objects.equals(stored.getDefinition(), candidate.getDefinition())) {
                return true;
            }
            // 定義キーが違う場合は、AE2詳細オブジェクト自身の比較へ委譲します。
            return stored.equals(candidate);
        } catch (RuntimeException exception) {
            // アドオン独自デコーダの比較失敗で登録処理全体を止めないようにします。
            LOG.warn("Decoded pattern comparison failed (stored={}, candidate={}, storedType={}, candidateType={})",
                    describeStack(storedStack), describeStack(candidateStack),
                    stored.getClass().getName(), candidate.getClass().getName(), exception);
            return false;
        }
    }

    private static IPatternDetails decode(ItemStack stack, Level level) {
        // Levelがない場合はAE2デコーダを呼ばず、候補を未デコード扱いにします。
        if (level == null) {
            return null;
        }
        try {
            return PatternDetailsHelper.decodePattern(stack, level);
        } catch (RuntimeException exception) {
            // 壊れたNBTや任意アドオンのデコーダ例外を安全に吸収します。
            LOG.warn("Pattern decode failed during duplicate check: {}", describeStack(stack), exception);
            return null;
        }
    }

    public static String describeStack(ItemStack stack) {
        // nullと空スタックはログ上で区別できる文字列にします。
        if (stack == null) {
            return "null";
        }
        if (stack.isEmpty()) {
            return "empty";
        }
        String itemId = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        // 1.20.1には2.0.7側のhashItemAndComponentsがないため、同じ識別材料を標準Javaでハッシュします。
        int stackHash = Objects.hash(stack.getItem(), stack.getTag());
        String hash = Integer.toUnsignedString(stackHash, HASH_RADIX);
        return itemId + "#" + hash;
    }

    public static String describeOccupiedStacks(InternalInventory inventory, int maxEntries) {
        // 対象インベントリがない場合は診断用の固定値を返します。
        if (inventory == null) {
            return "null-inventory";
        }
        StringBuilder result = new StringBuilder();
        int described = 0;
        // インベントリ全体を確認し、ログ出力数だけを上限として切り詰めます。
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            // 空スロットはログへ含めません。
            if (stack.isEmpty()) {
                continue;
            }
            // 指定された上限を超える場合は残りを省略します。
            if (described >= Math.max(1, maxEntries)) {
                result.append(", ...");
                break;
            }
            // 複数項目はカンマで区切ってスロット番号と内容を表示します。
            if (described > 0) {
                result.append(',');
            }
            result.append(slot).append('=').append(describeStack(stack));
            described++;
        }
        // 1件もない場合はemptyと表示します。
        return described == 0 ? "empty" : result.toString();
    }

    public record CheckResult(
            boolean duplicate,
            int matchedSlot,
            MatchMethod matchMethod,
            int occupiedSlots,
            int undecodableSlots) {
    }

    public enum MatchMethod {
        NONE,
        EXACT_STACK,
        DECODED_DEFINITION,
        CLOSED_LOOP_PAYLOAD
    }
}
