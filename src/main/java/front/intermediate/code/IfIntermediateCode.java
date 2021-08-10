package front.intermediate.code;

public class IfIntermediateCode extends OperationIntermediateCode {
    protected int gotoIndex;
    protected boolean positive;

    public IfIntermediateCode(String operator, String left, String right) {
        super(operator, left, right);
        positive = true;
    }

    public void setGotoIndex(int gotoIndex) {
        this.gotoIndex = gotoIndex;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    @Override
    public String toString() {
        return "if " + (positive ? "" : "not ") + left + " " + operator + " " + right + " goto " + gotoIndex;
    }
}
