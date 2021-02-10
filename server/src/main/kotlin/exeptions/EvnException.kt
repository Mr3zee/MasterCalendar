package exeptions

class EvnException(varName: String) : BaseException() {
    override val exceptionName = "Environmental Exception"
    override val exceptionDescription = "Variable not found: $varName"
}