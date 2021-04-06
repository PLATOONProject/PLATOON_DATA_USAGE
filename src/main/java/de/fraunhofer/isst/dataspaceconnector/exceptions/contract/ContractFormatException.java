package de.fraunhofer.isst.dataspaceconnector.exceptions.contract;

/**
 * Thrown to indicate that the contract format did not match expectations.
 */
public class ContractFormatException extends IllegalArgumentException {
    //Default serial version uid
    private static final long serialVersionUID = 1L;

    /**
     * Construct a ContractFormatException with the specified detail message.
     *
     * @param msg The detail message.
     */
    public ContractFormatException(String msg) {
        super(msg);
    }

    /**
     * Construct a ContractFormatException with the specified detail message and cause.
     *
     * @param msg   The detail message.
     * @param cause The cause.
     */
    public ContractFormatException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
