package exnihiloauto.fabric;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class MachineScreen extends AbstractContainerScreen<MachineMenu> {
    public MachineScreen(MachineMenu menu,Inventory inventory,Component title){super(menu,inventory,title,230,229);inventoryLabelX=35;inventoryLabelY=133;titleLabelX=10;titleLabelY=7;}
    public void extractBackground(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        super.extractBackground(g,mouseX,mouseY,delta);
        int x=leftPos,y=topPos;
        g.fill(x,y,x+imageWidth,y+imageHeight,0xff252b32);
        g.fill(x+2,y+2,x+imageWidth-2,y+imageHeight-2,0xffd5d5d5);
        g.fill(x+7,y+20,x+223,y+104,0xffb8bec3);
        for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff454c55);g.fill(x+slot.x,y+slot.y,x+slot.x+17,y+slot.y+17,0xfff0f0f0);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff9299a0);}
        g.fill(x+80,y+42,x+130,y+51,0xff454c55);
        g.fill(x+81,y+43,x+81+48*menu.progress()/1000,y+50,0xff4a9876);
        g.fill(x+10,y+121,x+220,y+129,0xff454c55);
        g.fill(x+11,y+122,x+11+(int)(208L*menu.energy()/Math.max(1,menu.capacity())),y+128,0xffdca735);
    }
    protected void extractLabels(GuiGraphicsExtractor g,int mouseX,int mouseY){
        super.extractLabels(g,mouseX,mouseY);
        text(g,"Input",12,24);text(g,switch(menu.kind()){case SIEVE->"Mesh";case HAMMER->"Hammer";case SILKER->"Worm";},48,24);
        text(g,"Fuel",12,68);text(g,"Upgrades",49,68);text(g,"Output",144,16);
        text(g,(menu.progress()/10)+"%",89,54);
        String[] states={"Ready","Working","Add input","Add mesh / tool / worm","Needs reinforcement","Add fuel","Output full","Redstone pause"};
        text(g,states[Math.clamp(menu.status(),0,states.length-1)],10,107);
        if(menu.kind()==MachineKind.SILKER)text(g,"Worm uses: "+menu.wormCharges(),118,94);
    }
    private void text(GuiGraphicsExtractor g,String s,int x,int y){g.text(font,s,x,y,0xff303740,false);}
    public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        super.extractRenderState(g,mouseX,mouseY,delta);
        if(mouseX>=leftPos+10&&mouseX<leftPos+220&&mouseY>=topPos+121&&mouseY<topPos+129)g.setTooltipForNextFrame(Component.literal(menu.energy()+" / "+menu.capacity()+" energy"),mouseX,mouseY);
    }
}
