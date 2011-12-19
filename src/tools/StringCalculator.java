package tools;

import java.util.Stack;

public class StringCalculator {

    private static Stack stack = new Stack();

    private static int operatorPriority(char operator) {
        if (operator == '(') {
            return 0;
        }
        if (operator == '+' || operator == '-') {
            return 1;
        }
        if (operator == '*' || operator == '/') {
            return 2;
        }
        return 3;
    }

    public static boolean isOperator(char ch) {
        return (ch == '+' || ch == '-' || ch == '*' || ch == '/');
    }

    public static boolean isNumeric(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    public static String postfix(String expression) throws Exception {
        StringBuilder sb = new StringBuilder();
        char[] exp;
        char ch;

        if (expression == null) {
            throw new NullPointerException("expression is null");
        }

        exp = expression.toCharArray();
        for (int i = 0; i < exp.length; i++) {
            if (exp[i] == '(') {
                stack.push(exp[i]);

            } else if (exp[i] == ')') {
                while ((ch = (Character) stack.pop()) != '(') {
                    sb.append(ch);
                    sb.append(' ');
                }

            } else if (isOperator(exp[i])) {
                while (!stack.isEmpty() && operatorPriority((Character) stack.peek()) >= operatorPriority(exp[i])) {
                    sb.append(stack.pop());
                    sb.append(' ');
                }
                stack.push(exp[i]);

            } else if (isNumeric(exp[i])) {
                do {
                    sb.append(exp[i++]);
                } while (i < exp.length && isNumeric(exp[i]));
                sb.append(' ');
                i--;
            }
        }

        while (!stack.isEmpty()) {
            sb.append(stack.pop());
            sb.append(' ');
        }

        return sb.toString();
    }

    // ??????????? ???
    public static int postfixCalc(String expression) throws Exception {
        char[] exp;
        int num;

        if (expression == null) {
            throw new NullPointerException("expression is null");
        }
        
        exp = expression.toCharArray();
        
        for (int i = 0; i < exp.length; i++) {
            if (isNumeric(exp[i])) {
                num = 0;

                do {
                    num = num * 10 + exp[i++] - '0';
                } while (i < exp.length && isNumeric(exp[i]));
                stack.push(num);
                i--;

            } else if (exp[i] == '+') {
                stack.push((Integer) stack.pop() + (Integer) stack.pop());

            } else if (exp[i] == '*') {
                stack.push((Integer) stack.pop() * (Integer) stack.pop());

            } else if (exp[i] == '-') {
                num = (Integer) stack.pop();
                stack.push((Integer) stack.pop() - num);

            } else if (exp[i] == '/') {
                num = (Integer) stack.pop();
                stack.push((Integer) stack.pop() / num);
            }
        }

        return (Integer) stack.pop();
    }
}