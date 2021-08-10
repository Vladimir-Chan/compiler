package front.intermediate.code;

public class JumpOperationIntermediateCode extends IntermediateCode {
    private String address;

    public JumpOperationIntermediateCode() {
        super("goto");
    }

    public JumpOperationIntermediateCode(String address) {
        super("goto");
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "goto " + address;
    }
}
