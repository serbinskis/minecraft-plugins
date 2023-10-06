package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.advancements.AdvancementNode;

public class AdvancementVisibilityEvaluator {

    private static final int VISIBILITY_DEPTH = 2;

    public AdvancementVisibilityEvaluator() {}

    private static AdvancementVisibilityEvaluator.b evaluateVisibilityRule(Advancement advancement, boolean flag) {
        Optional<AdvancementDisplay> optional = advancement.display();

        return optional.isEmpty() ? AdvancementVisibilityEvaluator.b.HIDE : (flag ? AdvancementVisibilityEvaluator.b.SHOW : (((AdvancementDisplay) optional.get()).isHidden() ? AdvancementVisibilityEvaluator.b.HIDE : AdvancementVisibilityEvaluator.b.NO_CHANGE));
    }

    private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.b> stack) {
        for (int i = 0; i <= 2; ++i) {
            AdvancementVisibilityEvaluator.b advancementvisibilityevaluator_b = (AdvancementVisibilityEvaluator.b) stack.peek(i);

            if (advancementvisibilityevaluator_b == AdvancementVisibilityEvaluator.b.SHOW) {
                return true;
            }

            if (advancementvisibilityevaluator_b == AdvancementVisibilityEvaluator.b.HIDE) {
                return false;
            }
        }

        return false;
    }

    private static boolean evaluateVisibility(AdvancementNode advancementnode, Stack<AdvancementVisibilityEvaluator.b> stack, Predicate<AdvancementNode> predicate, AdvancementVisibilityEvaluator.a advancementvisibilityevaluator_a) {
        boolean flag = predicate.test(advancementnode);
        AdvancementVisibilityEvaluator.b advancementvisibilityevaluator_b = evaluateVisibilityRule(advancementnode.advancement(), flag);
        boolean flag1 = flag;

        stack.push(advancementvisibilityevaluator_b);

        AdvancementNode advancementnode1;

        for (Iterator iterator = advancementnode.children().iterator(); iterator.hasNext(); flag1 |= evaluateVisibility(advancementnode1, stack, predicate, advancementvisibilityevaluator_a)) {
            advancementnode1 = (AdvancementNode) iterator.next();
        }

        boolean flag2 = flag1 || evaluateVisiblityForUnfinishedNode(stack);

        stack.pop();
        advancementvisibilityevaluator_a.accept(advancementnode, flag2);
        return flag1;
    }

    public static void evaluateVisibility(AdvancementNode advancementnode, Predicate<AdvancementNode> predicate, AdvancementVisibilityEvaluator.a advancementvisibilityevaluator_a) {
        AdvancementNode advancementnode1 = advancementnode.root();
        Stack<AdvancementVisibilityEvaluator.b> stack = new ObjectArrayList();

        for (int i = 0; i <= 2; ++i) {
            stack.push(AdvancementVisibilityEvaluator.b.NO_CHANGE);
        }

        evaluateVisibility(advancementnode1, stack, predicate, advancementvisibilityevaluator_a);
    }

    private static enum b {

        SHOW, HIDE, NO_CHANGE;

        private b() {}
    }

    @FunctionalInterface
    public interface a {

        void accept(AdvancementNode advancementnode, boolean flag);
    }
}
