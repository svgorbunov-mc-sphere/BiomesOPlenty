package biomesoplenty.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiListButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import biomesoplenty.common.world.BOPWorldSettings;
import biomesoplenty.common.world.BOPWorldSettings.BiomeSize;
import biomesoplenty.common.world.BOPWorldSettings.LandMassScheme;
import biomesoplenty.common.world.BOPWorldSettings.RainfallVariationScheme;
import biomesoplenty.common.world.BOPWorldSettings.TemperatureVariationScheme;

import com.google.common.base.Predicate;
import com.google.common.primitives.Floats;

@SideOnly(Side.CLIENT)
public class GuiBOPConfigureWorld extends GuiScreen implements GuiSlider.FormatHelper, GuiBOPPageList.GuiResponder
{
    private GuiCreateWorld parentScreen;
    
    protected String screenTitle = "Customize World Settings";
    protected String pageInfo = "Page 1 of 3";
    protected String page0Title = "Basic Settings";
    protected String[] pageNames;
    
    private GuiBOPPageManager pageManager;
    
    //Navigation buttons, required on all pages
    private GuiButton doneButton;
    private GuiButton defaultsButton;
    private GuiButton prevButton;
    private GuiButton nextButton;
    private GuiButton yesButton;
    private GuiButton noButton;
    
    private int modalAction = 0;
    private boolean field_175340_C = false;
    private Predicate validFloatPredicate = new Predicate()
    {
        public boolean tryParseValidFloat(String p_178956_1_)
        {
            Float f = Floats.tryParse(p_178956_1_);
            return p_178956_1_.length() == 0 || f != null && Floats.isFinite(f.floatValue()) && f.floatValue() >= 0.0F;
        }
        @Override
        public boolean apply(Object p_apply_1_)
        {
            return this.tryParseValidFloat((String)p_apply_1_);
        }
    };
    
    private BOPWorldSettings settings;
    

    public GuiBOPConfigureWorld(GuiScreen parentScreen, String settingsStringIn)
    {
        this.parentScreen = (GuiCreateWorld)parentScreen;
        
        if (settingsStringIn.isEmpty())
        {        
            this.settings = new BOPWorldSettings();
        } else {
            this.settings = new BOPWorldSettings(settingsStringIn);
        }
    }

    private static enum Actions
    {
        PREVIOUS (301),
        NEXT (302),
        DEFAULTS (303),
        DONE (304),
        YES (305),
        NO (306);
        
        private int id;
        
        private Actions(int id)
        {
            this.id = id;      
        }
        public int getId()
        {
            return this.id;
        }
        
        public static Actions fromId(int id)
        {
            for (Actions action : Actions.values())
            {
                if (action.id == id)
                {
                    return action;
                }
            }
            return null;
        }
    }

    @Override
    public void initGui()
    {
        this.screenTitle = I18n.format("options.customizeTitle");
        
        this.buttonList.clear();
        this.buttonList.add(this.prevButton = new GuiButton(Actions.PREVIOUS.getId(), 20, 5, 80, 20, I18n.format("createWorld.customize.custom.prev")));
        this.buttonList.add(this.nextButton = new GuiButton(Actions.NEXT.getId(), this.width - 100, 5, 80, 20, I18n.format("createWorld.customize.custom.next")));
        this.buttonList.add(this.defaultsButton = new GuiButton(Actions.DEFAULTS.getId(), this.width / 2 - 187, this.height - 27, 90, 20, I18n.format("createWorld.customize.custom.defaults")));
        this.buttonList.add(this.doneButton = new GuiButton(Actions.DONE.getId(), this.width / 2 + 98, this.height - 27, 90, 20, I18n.format("gui.done")));
       
        this.yesButton = new GuiButton(Actions.YES.getId(), this.width / 2 - 55, 160, 50, 20, I18n.format("gui.yes"));
        this.yesButton.visible = false;
        this.buttonList.add(this.yesButton);
        
        this.noButton = new GuiButton(Actions.NO.getId(), this.width / 2 + 5, 160, 50, 20, I18n.format("gui.no"));
        this.noButton.visible = false;
        this.buttonList.add(this.noButton);
        
        this.setupPages();
        
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();
        this.pageManager.getActivePage().handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.modalAction == 0 && !this.field_175340_C)
        {
            this.pageManager.getActivePage().mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);

        if (this.field_175340_C)
        {
            this.field_175340_C = false;
        }
        else if (this.modalAction == 0)
        {
            this.pageManager.getActivePage().mouseReleased(mouseX, mouseY, state);
        }
    }

    
    private static enum GuiEntries
    {
        TEMP_SCHEME (101),
        GENERATE_BOP_GEMS (102),
        AMPLITUDE (104),
        BIOME_SIZE (105),
        LAND_SCHEME (106),
        RAIN_SCHEME (107);
        
        private int id;
        
        private GuiEntries(int id)
        {
            this.id = id;      
        }
        public int getId()
        {
            return this.id;
        }
        
        public static GuiEntries fromId(int id)
        {
            for (GuiEntries entry : GuiEntries.values())
            {
                if (entry.id == id)
                {
                    return entry;
                }
            }
            return null;
        }
    }
    
    
    private void setupPages()
    {
        this.pageNames = new String[3];
        
        this.pageNames[0] = "World";
        GuiBOPPageList.GuiFieldEntry[] page0Fields = new GuiBOPPageList.GuiFieldEntry[] {
            new GuiBOPPageList.GuiEnumButtonEntry<BiomeSize>(GuiEntries.BIOME_SIZE.getId(), "Biome Size: %s", true, this.settings.biomeSize),
            new GuiBOPPageList.GuiEnumButtonEntry<LandMassScheme>(GuiEntries.LAND_SCHEME.getId(), "Land Mass: %s", true, this.settings.landScheme),
            new GuiBOPPageList.GuiEnumButtonEntry<TemperatureVariationScheme>(GuiEntries.TEMP_SCHEME.getId(), "Temperature: %s", true, this.settings.tempScheme),
            new GuiBOPPageList.GuiEnumButtonEntry<RainfallVariationScheme>(GuiEntries.RAIN_SCHEME.getId(), "Rainfall: %s", true, this.settings.rainScheme),
            new GuiBOPPageList.GuiSlideEntry(GuiEntries.AMPLITUDE.getId(), "Amplitude", true, this, 0.2F, 3.0F, this.settings.amplitude)
        };
        
        this.pageNames[1] = "Biomes";
        GuiBOPPageList.GuiFieldEntry[] page1Fields = new GuiBOPPageList.GuiFieldEntry[] {

        };
        
        this.pageNames[2] = "Features";
        GuiBOPPageList.GuiFieldEntry[] page2Fields = new GuiBOPPageList.GuiFieldEntry[] {
            new GuiBOPPageList.GuiButtonEntry(GuiEntries.GENERATE_BOP_GEMS.getId(), "Generate BOP gems", true, this.settings.generateBopGems)
        };
        
        this.pageManager = new GuiBOPPageManager(createTableForFields(page0Fields, page1Fields, page2Fields));
        this.pageManager.setup();
        
        this.showNewPage();
    }
    
    private GuiBOPPageTable[] createTableForFields(GuiBOPPageList.GuiFieldEntry[]... fieldGroup)
    {
        GuiBOPPageTable[] output = new GuiBOPPageTable[fieldGroup.length];
        
        for (int i = 0; i < fieldGroup.length; i++)
        {
            GuiBOPPageList.GuiFieldEntry[] fields = fieldGroup[i];
            output[i] = new GuiBOPPageTable(this.width, this.height, 32, this.height - 32, 25, i, this, fields);
        }
        
        return output;
    }

    public String serialize()
    {
        return this.settings.toString().replace("\n", "");
    }
    


    @Override
    public String getText(int id, String name, float value)
    {
        return name + ": " + this.stringFormatFloat(id, value);
    }

    private String stringFormatFloat(int fieldId, float value)
    {
        GuiEntries entry = GuiEntries.fromId(fieldId);
        if (entry == null) {return "";}
        
        switch (entry)
        {
            case AMPLITUDE:
                return String.format("%5.3f", new Object[] {Float.valueOf(value)});
            default:
                return "";
        }
    }
    
    
    

    @Override
    public void handleEnumSelection(int fieldId, int ordinal)
    {
        GuiEntries entry = GuiEntries.fromId(fieldId);
        if (entry == null) {return;}
        
        switch (entry)
        {
            case LAND_SCHEME:
                LandMassScheme[] land_values = LandMassScheme.values();
                this.settings.landScheme = land_values[ordinal % land_values.length];
                break;
            case TEMP_SCHEME:
                TemperatureVariationScheme[] temp_values = TemperatureVariationScheme.values();
                this.settings.tempScheme = temp_values[ordinal % temp_values.length];
                break;
            case RAIN_SCHEME:
                RainfallVariationScheme[] rain_values = RainfallVariationScheme.values();
                this.settings.rainScheme = rain_values[ordinal % rain_values.length];
                break;
            case BIOME_SIZE:
                BiomeSize[] size_values = BiomeSize.values();
                this.settings.biomeSize = size_values[ordinal % size_values.length];
                break;
            default:
                break;
        }
        
        System.out.println("settings currently: "+this.settings.toJson());

    }

    @Override    
    public void handleBooleanSelection(int fieldId, boolean value)
    {
        GuiEntries entry = GuiEntries.fromId(fieldId);
        if (entry == null) {return;}
        
        switch (entry)
        {
            case GENERATE_BOP_GEMS:
                this.settings.generateBopGems = value;
                break;
            default:
                break;
        }
        
        System.out.println("settings currently: "+this.settings.toJson());

    }


    @Override
    public void handleFloatSelection(int fieldId, float value)
    {
        GuiEntries entry = GuiEntries.fromId(fieldId);
        if (entry == null) {return;}
        
        switch (entry)
        {
            case AMPLITUDE:
                this.settings.amplitude = value;
                break;
            default:
                break;
        }
        
        System.out.println("settings currently: "+this.settings.toJson());

    }
    

    @Override
    public void handleStringSelection(int fieldId, String value)
    {
        ;
    }
    
    
    @Override
    public void handleIntSelection(int fieldId, int value)
    {
        ;
    }
    
    
    // These 3 are the original functions required by GuiPageButtonList.GuiResponder - just pass off to the better named functions
    @Override
    public void func_175319_a(int fieldId, String value) {this.handleStringSelection(fieldId, value);}
    @Override
    public void onTick(int fieldId, float value) {this.handleFloatSelection(fieldId, value);}
    @Override 
    public void func_175321_a(int fieldId, boolean value) {this.handleBooleanSelection(fieldId, value);}
    

    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            Actions action = Actions.fromId(button.id);
            if (action == null) {return;}
            switch (action)
            {
                case DONE:
                    this.parentScreen.chunkProviderSettingsJson = this.settings.toJson();
                    System.out.println("settings sent to world screen "+this.parentScreen.chunkProviderSettingsJson);
                    this.mc.displayGuiScreen(this.parentScreen);
                    break;
                    
                case PREVIOUS:
                    this.pageManager.gotToPrevPage(); // prev page
                    this.showNewPage();
                    break;
                    
                case NEXT:
                    this.pageManager.goToNextPage(); // next page
                    this.showNewPage();
                    break;
                    
                case DEFAULTS:
                    this.confirmSetDefaults(button.id);
                    break;
                    
                case YES:
                    this.handleModalClose();
                    break;
                    
                case NO:
                    this.modalAction = 0;
                    this.handleModalClose();
            }
        }
    }

    
    private void doSetDefaults()
    {
        this.settings.setDefault();
        this.setupPages();
    }

    private void confirmSetDefaults(int actionId)
    {
        this.modalAction = actionId;
        this.showModal(true);
    }

    private void handleModalClose() throws IOException
    {
        Actions action = Actions.fromId(this.modalAction);
        if (action != null) {
            switch (action)
            {
                case DONE:
                    this.actionPerformed((GuiListButton)this.pageManager.getActivePage().getGui(300));
                    break;
                case DEFAULTS:
                    this.doSetDefaults();
                    break;
                default:
                    break;
            }
        }
        this.modalAction = 0;
        this.field_175340_C = true;
        this.showModal(false);
    }

    private void showModal(boolean flag)
    {
        this.yesButton.visible = flag;
        this.noButton.visible = flag;
        this.doneButton.enabled = !flag;
        this.prevButton.enabled = !flag;
        this.nextButton.enabled = !flag;
        this.defaultsButton.enabled = !flag;
    }

    private void showNewPage()
    {
        this.prevButton.enabled = this.pageManager.getActivePage().pageNumber != 0;
        this.nextButton.enabled = this.pageManager.getActivePage().pageNumber != this.pageManager.getNumPages() - 1;
        this.pageInfo = I18n.format("book.pageIndicator", new Object[] {Integer.valueOf(this.pageManager.getActivePage().pageNumber + 1), Integer.valueOf(this.pageManager.getNumPages())});
        this.page0Title = this.pageNames[this.pageManager.getActivePage().pageNumber];
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (this.modalAction == 0)
        {
            switch (keyCode)
            {
                case 200:
                    this.func_175327_a(1.0F);
                    break;
                case 208:
                    this.func_175327_a(-1.0F);
                    break;
                default:
                    this.pageManager.getActivePage().keyTyped(typedChar, keyCode);
            }
        }
    }

    private void func_175327_a(float p_175327_1_)
    {
        Gui gui = this.pageManager.getActivePage().getFocusedGuiElement();

        if (gui instanceof GuiTextField)
        {
            float f1 = p_175327_1_;

            if (GuiScreen.isShiftKeyDown())
            {
                f1 = p_175327_1_ * 0.1F;

                if (GuiScreen.isCtrlKeyDown())
                {
                    f1 *= 0.1F;
                }
            }
            else if (GuiScreen.isCtrlKeyDown())
            {
                f1 = p_175327_1_ * 10.0F;

                if (GuiScreen.isAltKeyDown())
                {
                    f1 *= 10.0F;
                }
            }

            GuiTextField guitextfield = (GuiTextField)gui;
            Float f2 = Floats.tryParse(guitextfield.getText());

            if (f2 != null)
            {
                f2 = Float.valueOf(f2.floatValue() + f1);
                int i = guitextfield.getId();
                String s = this.stringFormatFloat(guitextfield.getId(), f2.floatValue());
                guitextfield.setText(s);
                this.func_175319_a(i, s);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.pageManager.getActivePage().drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 2, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.pageInfo, this.width / 2, 12, 16777215);
        this.drawCenteredString(this.fontRendererObj, this.page0Title, this.width / 2, 22, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.modalAction != 0)
        {
            drawRect(0, 0, this.width, this.height, Integer.MIN_VALUE);
            this.drawHorizontalLine(this.width / 2 - 91, this.width / 2 + 90, 99, -2039584);
            this.drawHorizontalLine(this.width / 2 - 91, this.width / 2 + 90, 185, -6250336);
            this.drawVerticalLine(this.width / 2 - 91, 99, 185, -2039584);
            this.drawVerticalLine(this.width / 2 + 90, 99, 185, -6250336);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            this.mc.getTextureManager().bindTexture(optionsBackground);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double)(this.width / 2 - 90), 185.0D, 0.0D).tex(0.0D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos((double)(this.width / 2 + 90), 185.0D, 0.0D).tex(5.625D, 2.65625D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos((double)(this.width / 2 + 90), 100.0D, 0.0D).tex(5.625D, 0.0D).color(64, 64, 64, 64).endVertex();
            worldrenderer.pos((double)(this.width / 2 - 90), 100.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 64).endVertex();
            tessellator.draw();
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirmTitle", new Object[0]), this.width / 2, 105, 16777215);
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirm1", new Object[0]), this.width / 2, 125, 16777215);
            this.drawCenteredString(this.fontRendererObj, I18n.format("createWorld.customize.custom.confirm2", new Object[0]), this.width / 2, 135, 16777215);
            this.yesButton.drawButton(this.mc, mouseX, mouseY);
            this.noButton.drawButton(this.mc, mouseX, mouseY);
        }
    }
}