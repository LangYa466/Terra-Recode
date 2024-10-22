/*
 * Terra Hacked Client
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.Terra
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiButton
import net.ccbluex.liquidbounce.api.minecraft.client.gui.IGuiScreen
import net.ccbluex.liquidbounce.api.util.WrappedGuiScreen
import net.ccbluex.liquidbounce.api.util.WrappedGuiSlot
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import org.apache.commons.io.IOUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*
import java.util.zip.ZipFile

class GuiScripts(private val prevGui: IGuiScreen) : WrappedGuiScreen() {

    private lateinit var list: GuiList

    override fun initGui() {
        list = GuiList(representedScreen)
        list.represented.registerScrollButtons(7, 8)
        list.elementClicked(-1, false, 0, 0)

        val j = 22
        representedScreen.buttonList.add(classProvider.createGuiButton(0, representedScreen.width - 80, representedScreen.height - 65, 70, 20, "Back"))
        representedScreen.buttonList.add(classProvider.createGuiButton(1, representedScreen.width - 80, j + 24, 70, 20, "Import"))
        representedScreen.buttonList.add(classProvider.createGuiButton(2, representedScreen.width - 80, j + 24 * 2, 70, 20, "Delete"))
        representedScreen.buttonList.add(classProvider.createGuiButton(3, representedScreen.width - 80, j + 24 * 3, 70, 20, "Reload"))
        representedScreen.buttonList.add(classProvider.createGuiButton(4, representedScreen.width - 80, j + 24 * 4, 70, 20, "Folder"))
        representedScreen.buttonList.add(classProvider.createGuiButton(5, representedScreen.width - 80, j + 24 * 5, 70, 20, "Docs"))
        representedScreen.buttonList.add(classProvider.createGuiButton(6, representedScreen.width - 80, j + 24 * 6, 70, 20, "Find Scripts"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        representedScreen.drawBackground(0)

        list.represented.drawScreen(mouseX, mouseY, partialTicks)

        Fonts.font40.drawCenteredString("§9§lScripts", representedScreen.width / 2.0f, 28.0f, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: IGuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> try {
                val file = MiscUtils.openFileChooser() ?: return
                val fileName = file.name

                if (fileName.endsWith(".js")) {
                    Terra.scriptManager.importScript(file)

                    Terra.clickGui = ClickGui()
                    Terra.fileManager.loadConfig(Terra.fileManager.clickGuiConfig)
                    return
                } else if (fileName.endsWith(".zip")) {
                    val zipFile = ZipFile(file)
                    val entries = zipFile.entries()
                    val scriptFiles = ArrayList<File>()

                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        val entryName = entry.name
                        val entryFile = File(Terra.scriptManager.scriptsFolder, entryName)

                        if (entry.isDirectory) {
                            entryFile.mkdir()
                            continue
                        }

                        val fileStream = zipFile.getInputStream(entry)
                        val fileOutputStream = FileOutputStream(entryFile)

                        IOUtils.copy(fileStream, fileOutputStream)
                        fileOutputStream.close()
                        fileStream.close()

                        if (!entryName.contains("/"))
                            scriptFiles.add(entryFile)
                    }

                    scriptFiles.forEach { scriptFile -> Terra.scriptManager.loadScript(scriptFile) }

                    Terra.clickGui = ClickGui()
                    Terra.fileManager.loadConfig(Terra.fileManager.clickGuiConfig)
                    Terra.fileManager.loadConfig(Terra.fileManager.hudConfig)
                    return
                }

                MiscUtils.showErrorPopup("Wrong file extension.", "The file extension has to be .js or .zip")
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while importing a script.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }

            2 -> try {
                if (list.getSelectedSlot() != -1) {
                    val script = Terra.scriptManager.scripts[list.getSelectedSlot()]

                    Terra.scriptManager.deleteScript(script)

                    Terra.clickGui = ClickGui()
                    Terra.fileManager.loadConfig(Terra.fileManager.clickGuiConfig)
                    Terra.fileManager.loadConfig(Terra.fileManager.hudConfig)
                }
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while deleting a script.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
            3 -> try {
                Terra.scriptManager.reloadScripts()
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while reloading all scripts.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
            4 -> try {
                Desktop.getDesktop().open(Terra.scriptManager.scriptsFolder)
            } catch (t: Throwable) {
                ClientUtils.getLogger().error("Something went wrong while trying to open your scripts folder.", t)
                MiscUtils.showErrorPopup(t.javaClass.name, t.message)
            }
            5 -> try {
                Desktop.getDesktop().browse(URL("https://liquidbounce.net/docs/ScriptAPI/Getting%20Started").toURI())
            } catch (ignored: Exception) { }

            6 -> try {
                Desktop.getDesktop().browse(URL("https://forum.ccbluex.net/viewforum.php?id=16").toURI())
            } catch (ignored: Exception) { }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        list.represented.handleMouseInput()
    }

    private inner class GuiList(gui: IGuiScreen) :
            WrappedGuiSlot(mc, gui.width, gui.height, 40, gui.height - 40, 30) {

        private var selectedSlot = 0

        override fun isSelected(id: Int) = selectedSlot == id

        internal fun getSelectedSlot() = if (selectedSlot > Terra.scriptManager.scripts.size) -1 else selectedSlot

        override fun getSize() = Terra.scriptManager.scripts.size

        public override fun elementClicked(id: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = id
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val script = Terra.scriptManager.scripts[id]
            Fonts.font40.drawCenteredString("§9" + script.scriptName + " §7v" + script.scriptVersion, representedScreen.width / 2.0f, y + 2.0f, Color.LIGHT_GRAY.rgb)
            Fonts.font40.drawCenteredString("by §c" + script.scriptAuthors.joinToString(", "), representedScreen.width / 2.0f, y + 15.0f, Color.LIGHT_GRAY.rgb)
        }

        override fun drawBackground() { }
    }
}