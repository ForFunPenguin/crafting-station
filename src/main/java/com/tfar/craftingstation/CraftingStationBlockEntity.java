package com.tfar.craftingstation;

import com.tfar.craftingstation.util.CraftingStationItemHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CraftingStationBlockEntity extends TileEntity implements INamedContainerProvider {

  public CraftingStationItemHandler input;
  public ItemStackHandler output;

  public CraftingStationBlockEntity() {
    super(CraftingStation.Objects.crafting_station_tile);
    this.input = new CraftingStationItemHandler(9);
    this.output = new ItemStackHandler();
  }

  @Nonnull
  @Override
  public CompoundNBT write(CompoundNBT tag) {
    CompoundNBT compound = this.input.serializeNBT();
    tag.put("inv", compound);
   // if (this.customName != null) {
   //   tag.putString("CustomName", ITextComponent.Serializer.toJson(this.customName));
  //  }
    return super.write(tag);
  }

  @Override
  public void read(CompoundNBT tag) {
    CompoundNBT invTag = tag.getCompound("inv");
    input.deserializeNBT(invTag);
  //  if (tag.contains("CustomName", 8)) {
  //    this.customName = ITextComponent.Serializer.fromJson(tag.getString("CustomName"));
 //   }
    super.read(tag);
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    return new TranslationTextComponent("title.crafting_station");
  }

  @Nullable
  @Override
  public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player) {
    return new CraftingStationContainer(id, playerInventory, world, pos,player);
  }
  /*private List<Listener> listeners = new ArrayList<>();

  public void addListener(Listener listener) {
    listeners.add(listener);
  }

  public void removeListener(Listener listener) {
    listeners.remove(listener);
  }

  public interface Listener {
  }*/

  @Override
  public CompoundNBT getUpdateTag()
  {
    return write(new CompoundNBT());    // okay to send entire inventory on chunk load
  }

  @Override
  public SUpdateTileEntityPacket getUpdatePacket()
  {
    return new SUpdateTileEntityPacket(getPos(), 1, getUpdateTag());
  }

  @Override
  public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet)
  {
    this.read(packet.getNbtCompound());
  }
}

