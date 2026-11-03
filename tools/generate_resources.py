from pathlib import Path
import json,shutil
root=Path(__file__).resolve().parents[1]/'src/main/resources'
up=root.parents[3]/'upstream-automation/1.21.4/src'
def put(path,value):
 p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def copy(src,dst):
 p=root/dst;p.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(src,p)
lang={'itemGroup.exnihiloauto':'Ex Nihilo Automation'}
for kind,center in [('sieve','exnihiloomnia:sieve_wood'),('hammer','exnihiloomnia:hammer_diamond'),('silker','exnihiloomnia:crook_bone')]:
 name='auto_'+kind
 model=json.loads((up/f'generated/resources/assets/exnihiloauto/models/block/{name}.json').read_text())
 put(Path(f'assets/exnihiloauto/models/block/{name}.json'),model)
 put(Path(f'assets/exnihiloauto/blockstates/{name}.json'),{'variants':{'':{'model':f'exnihiloauto:block/{name}'}}})
 put(Path(f'assets/exnihiloauto/models/item/{name}.json'),{'parent':f'exnihiloauto:block/{name}'})
 put(Path(f'assets/exnihiloauto/items/{name}.json'),{'model':{'type':'minecraft:model','model':f'exnihiloauto:item/{name}'}})
 put(Path(f'data/exnihiloauto/loot_table/blocks/{name}.json'),{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'exnihiloauto:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 put(Path(f'data/exnihiloauto/recipe/{name}.json'),{'type':'minecraft:crafting_shaped','category':'redstone','pattern':['IRI','RCR','IRI'],'key':{'I':'minecraft:iron_ingot','R':'minecraft:redstone','C':center},'result':{'id':f'exnihiloauto:{name}','count':1}})
 lang[f'block.exnihiloauto.{name}']='Auto '+kind.title()
for texture in ['auto_side','auto_top','auto_bottom']:
 copy(up/f'main/resources/assets/exnihiloauto/textures/block/{texture}.png',Path(f'assets/exnihiloauto/textures/block/{texture}.png'))
for name,material,texture in [('speed_upgrade','sugar','speed_upgrade'),('bonus_upgrade','emerald','bonus_upgrade'),('reinforced_upgrade','obsidian','reinforcement_upgrade')]:
 copy(up/f'main/resources/assets/exnihiloauto/textures/item/{texture}.png',Path(f'assets/exnihiloauto/textures/item/{name}.png'))
 put(Path(f'assets/exnihiloauto/models/item/{name}.json'),{'parent':'minecraft:item/generated','textures':{'layer0':f'exnihiloauto:item/{name}'}})
 put(Path(f'assets/exnihiloauto/items/{name}.json'),{'model':{'type':'minecraft:model','model':f'exnihiloauto:item/{name}'}})
 put(Path(f'data/exnihiloauto/recipe/{name}.json'),{'type':'minecraft:crafting_shaped','category':'misc','pattern':['IRI','RCR','IRI'],'key':{'I':'minecraft:iron_ingot','R':f'minecraft:{material}','C':'minecraft:redstone'},'result':{'id':f'exnihiloauto:{name}','count':1}})
 lang[f'item.exnihiloauto.{name}']=name.replace('_',' ').title()
for tier in ['wood','stone','iron','gold','diamond','netherite']:
 name='reinforced_hammer_'+tier
 # Use existing Omnia hammer artwork; no new image assets needed.
 put(Path(f'assets/exnihiloauto/models/item/{name}.json'),{'parent':'minecraft:item/handheld','textures':{'layer0':f'exnihiloomnia:item/hammer_{tier if tier!="netherite" else "diamond"}'}})
 put(Path(f'assets/exnihiloauto/items/{name}.json'),{'model':{'type':'minecraft:model','model':f'exnihiloauto:item/{name}'}})
 put(Path(f'data/exnihiloauto/recipe/{name}.json'),{'type':'minecraft:crafting_shaped','category':'equipment','pattern':[' O ','OHO',' O '],'key':{'O':'minecraft:obsidian','H':f'exnihiloomnia:hammer_{tier}'},'result':{'id':f'exnihiloauto:{name}','count':1}})
 lang[f'item.exnihiloauto.{name}']='Reinforced '+tier.title()+' Hammer'
put(Path('assets/exnihiloauto/lang/en_us.json'),lang)
put(Path('data/minecraft/tags/block/mineable/pickaxe.json'),{'replace':False,'values':['exnihiloauto:auto_'+s for s in ['sieve','hammer','silker']]})
print('Resources generated')
