package front.intermediate.code;

public class AssignmentIntermediateCode extends OperationIntermediateCode {
    protected String rs;

    public AssignmentIntermediateCode(String operator, String left, String right, String rs) {
        super(operator, left, right);
        this.rs = rs;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    @Override
    public String toString() {
        return rs + " = " + left + ' ' + (operator == null ? "" : operator) + ' ' + (right == null ? "" : right);
    }
}
