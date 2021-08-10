package front.intermediate.code;

public class OperationIntermediateCode extends IntermediateCode{
    protected final String left;
    protected final String right;

    public OperationIntermediateCode(String operator, String left, String right) {
        super(operator);
        this.left = left;
        this.right = right;
    }
}
