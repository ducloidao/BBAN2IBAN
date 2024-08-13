package nl.rabobank.investments.cactus.commons.bbanibanconverter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import nl.garvelink.iban.IBAN
import nl.garvelink.iban.IBANFields
import nl.rabobank.investments.cactus.commons.bbanibanconverter.BbanToIban.convertBBANToIBAN


abstract class IbanBbanConvertAction(val bankSwiftCode: String) : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        // Get all the required data from data keys
        val editor: Editor = event.getRequiredData(CommonDataKeys.EDITOR)
        val project: Project = event.getRequiredData(CommonDataKeys.PROJECT)
        val document: Document = editor.document


        // Work off of the primary caret to get the selection info
        val primaryCaret: Caret = editor.caretModel.primaryCaret
        val selectedText = primaryCaret.selectedText
        val start: Int = primaryCaret.getSelectionStart()
        val end: Int = primaryCaret.getSelectionEnd()
        if (!selectedText.isNullOrEmpty()) {
            val iban = convertBbanOrIban(selectedText)
            // Replace the selection with a fixed string.
            // Must do this document change in a write action context.
            WriteCommandAction.runWriteCommandAction(
                project
            ) { document.replaceString(start, end, iban) }
            // De-select the text range that was just replaced
            primaryCaret.removeSelection()
        }
    }

    override fun update(e: AnActionEvent) {
        // Get required data keys
        val project = e.getProject();
        val editor = e.getData(CommonDataKeys.EDITOR);
        val selectedText = editor?.caretModel?.primaryCaret?.selectedText
        if (!selectedText.isNullOrEmpty() && isEnableVisibleMenuItem(selectedText)) {
            val converted = convertBbanOrIban(selectedText)
            val bankType = bankTypeText(selectedText)
            e.presentation.text = "Convert $bankType to $converted"
            // Set visibility and enable only in case of existing project and editor and if a selection exists
            e.presentation.isEnabledAndVisible =
                project != null && editor != null && editor.selectionModel.hasSelection();
        } else {
            e.presentation.isEnabledAndVisible = false
        }
    }

    protected fun isEnableVisibleMenuItem(text: String): Boolean {
        return canConvertBBANToIBAN(text) || canConvertIbanToBban(text)
    }

    private fun bankTypeText(text: String): String =
        when {
            canConvertBBANToIBAN(text) -> "BBAN"
            canConvertIbanToBban(text) -> "IBAN"
            else -> ""
        }

    private fun canConvertBBANToIBAN(text: String): Boolean {
        return runCatching { convertBBANToIBAN(text, bankSwiftCode) }.fold(
            onSuccess = { true },
            onFailure = { false }
        )
    }

    private fun canConvertIbanToBban(text: String): Boolean {
        return runCatching { text.toIban() }.fold(
            onSuccess = {
                IBANFields.getBankIdentifier(it).map { it == bankSwiftCode }.orElse(false)
            },
            onFailure = { false })
    }

    protected fun convertBbanOrIban(text: String): String {
        return runCatching {
            text.toIban().toBban()
        }.recoverCatching {
            convertBBANToIBAN(text, bankSwiftCode)
        }.getOrElse { text }
    }

    protected fun IBAN.toBban(): String {
        val toPlainString = this.toPlainString()
        val bban = toPlainString.substring(toPlainString.length - BBAN_LENGTH_NETHERLANDS).trimStart('0')
        return bban
    }

    protected fun String.toIban() = IBAN.valueOf(this.eatUpAllSpace())


    private fun String.eatUpAllSpace() = this.filterNot { it.isWhitespace() }

    companion object {
        const val NL = "NL"
        const val BBAN_LENGTH_NETHERLANDS = 10
        const val RABO_SWIFT_CODE = "RABO"
        const val ING_SWIFT_CODE = "INGB"
        const val ABN_SWIFT_CODE = "ABNA"
    }
}