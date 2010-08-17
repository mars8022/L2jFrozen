import sys
from com.l2scoria.gameserver.ai import CtrlIntention
from com.l2scoria.gameserver.datatables import SkillTable
from com.l2scoria.gameserver.datatables.csv import DoorTable
from com.l2scoria.gameserver.managers import GrandBossManager
from com.l2scoria.gameserver.model.quest import State
from com.l2scoria.gameserver.model.quest import QuestState
from com.l2scoria.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2scoria.gameserver.network.serverpackets import SocialAction
from com.l2scoria.gameserver.network.serverpackets import Earthquake
from com.l2scoria.gameserver.network.serverpackets import PlaySound
from com.l2scoria.gameserver.network.serverpackets import SpecialCamera
from com.l2scoria.util.random import Rnd
from java.lang import System

KLEIN = 31540
HEART = 31385
STONE = 7267
VALAKAS = 29028

#DOGS
ONE = 29030
TWORIGHT = 29036
TWOLEFT = 29037

class Valakas(JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   ubitt = self.loadGlobalQuestVar("ubit")
   underatak = self.loadGlobalQuestVar("underattack")
   if ubitt == "1" :    
     temp = long(self.loadGlobalQuestVar("respawn")) - System.currentTimeMillis()
     if temp > 0 :
       self.deleteGlobalQuestVar("underattack")
       self.deleteGlobalQuestVar("life")
       self.deleteGlobalQuestVar("lasthit")
       print "Valakas:  dead"
       self.startQuestTimer("resp", temp, None, None)
     else :
       print "Valakas:  live"
       self.deleteGlobalQuestVar("life")
       self.deleteGlobalQuestVar("ubit")
       self.deleteGlobalQuestVar("lasthit")
       self.deleteGlobalQuestVar("respawn")
   elif underatak == "1" :
     print "Valakas:  under attack"
     self.deleteGlobalQuestVar("lasthit")
     self.deleteGlobalQuestVar("underattack")
   else :
     self.deleteGlobalQuestVar("life")
     print "Valakas: live"
   return

 def onAdvEvent (self,event,npc,player):
   if event == "prosnuca" :
     valik = self.addSpawn(VALAKAS,213004,-114890,-1635,30000,False,0)
     player.broadcastPacket(SocialAction(valik.getObjectId(),1))
     player.broadcastPacket(Earthquake(valik.getX(), valik.getY(), valik.getZ(),40,5))
     self.startQuestTimer("camera",2000, valik, player)
     self.startQuestTimer("camerg",22000, valik, player)
     DoorTable.getInstance().getDoor(24210004).closeMe()
     DoorTable.getInstance().getDoor(24210005).closeMe()
     DoorTable.getInstance().getDoor(24210006).closeMe()
     self.deleteGlobalQuestVar("access")
     self.saveGlobalQuestVar("underattack", "1")
     self.startQuestTimer("vkrovatku",1800000,valik,None)
   elif event == "camera" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,1,0,20000))
     self.startQuestTimer("camerb",2000, npc, player)
   elif event == "camerb" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-3500,22,-100,111,15000))
     self.startQuestTimer("camerc",4000, npc, player)
   elif event == "camerc" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-2700,190,15,1,20000))
     self.startQuestTimer("camerd",3000, npc, player)
   elif event == "camerd" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,14,0,20000))
     self.startQuestTimer("camere",3000, npc, player)
   elif event == "camere" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,15,0,20000))
     self.startQuestTimer("camerf",3000, npc, player)
   elif event == "camerf" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,1,0,20000))
   elif event == "camerg" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,1,0,25000))
     self.startQuestTimer("camerh",4000, npc, player)
   elif event == "camerh" :
     player.broadcastPacket(SpecialCamera(npc.getObjectId(),-1700,190,1,11,5000))
     npc.setTarget(npc)
     npc.doCast(SkillTable.getInstance().getInfo(4691,1))
   if event == "vkrovatku" :
     underatak = self.loadGlobalQuestVar("underattack")
     if underatak == "" :
       npc.deleteMe()
       self.deleteGlobalQuestVar("underattack")
       self.cancelQuestTimer("vkrovatku",npc,None)
     else :
       self.deleteGlobalQuestVar("underattack")
       self.startQuestTimer("lastchek",60000, npc, player)
   if event == "lastchek" :
     underatak = self.loadGlobalQuestVar("underattack")
     if underatak == "" :
       npc.deleteMe()
       self.deleteGlobalQuestVar("underattack")
       self.cancelQuestTimer("lastchek",npc,None)
     else :
       self.deleteGlobalQuestVar("underattack")
       self.startQuestTimer("vkrovatku",1800000,npc,None)
   elif event == "resp" :
     self.deleteGlobalQuestVar("ubit")
     self.cancelQuestTimer("resp",npc,None) 
   return
    
 def onAttack (self,npc,player,damage,isPet):
   self.saveGlobalQuestVar("underattack", "1")
   maxHp = npc.getMaxHp()
   nowHp = npc.getCurrentHp()
   if nowHp < maxHp*0.25:
     if (Rnd.get(100) < 50):
       if (Rnd.get(100) < 20):
         npc.setTarget(npc)
         npc.doCast(SkillTable.getInstance().getInfo(4690,1))
       elif (Rnd.get(100) < 15):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4680,1))
       elif (Rnd.get(100) < 25):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4685,1))
       elif (Rnd.get(100) < 10):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4688,1))
       elif (Rnd.get(100) < 35):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4683,1))
       else:
         if (Rnd.get(100) < 50):
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4681,1))
         else:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4682,1))  
     elif (Rnd.get(100) < 20):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4690,1))
     elif (Rnd.get(100) < 15):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4689,1))
     else:
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4684,1))
   elif nowHp < maxHp*0.5:
     if (Rnd.get(100) < 50):
       if (Rnd.get(100) < 5):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4690,1))
       elif (Rnd.get(100) < 10):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4689,1))
       elif (Rnd.get(100) < 15):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4685,1))
       elif (Rnd.get(100) < 30):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4688,1))
       elif (Rnd.get(100) < 20):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4683,1))
       else:
         if (Rnd.get(100) < 50):
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4681,1))
         else:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4682,1))
     elif (Rnd.get(100) < 5):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4690,1))
     elif (Rnd.get(100) < 10):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4689,1))
     else:
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4684,1))
   elif nowHp < maxHp*0.75:
     if (Rnd.get(100) < 50):
       if (Rnd.get(100) < 0):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4690,1))
       elif (Rnd.get(100) < 5):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4689,1))
       elif (Rnd.get(100) < 7):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4685,1))
       elif (Rnd.get(100) < 10):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4688,1))
       elif (Rnd.get(100) < 15):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4683,1))
       else:
         if (Rnd.get(100) < 50):
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4681,1))
         else:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4682,1))
     elif (Rnd.get(100) < 0):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4690,1))
     elif (Rnd.get(100) < 5):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4689,1))
     else:
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4684,1))
   else:
     if (Rnd.get(100) < 50):
       if (Rnd.get(100) < 0):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4690,1))
       elif (Rnd.get(100) < 5):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4689,1))
       elif (Rnd.get(100) < 7):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4685,1))
       elif (Rnd.get(100) < 10):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4688,1))
       elif (Rnd.get(100) < 15):
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4683,1))
       else:
         if (Rnd.get(100) < 50):
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4681,1))
         else:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4682,1))
     elif (Rnd.get(100) < 0):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4690,1))
     elif (Rnd.get(100) < 10):
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4689,1))
     else:
       npc.setTarget(player)
       npc.doCast(SkillTable.getInstance().getInfo(4684,1))
   return
        
 def onTalk (self,npc,player):
   st = player.getQuestState("valakas")  
   npcId = npc.getNpcId()
   underatak = self.loadGlobalQuestVar("underattack")
   ubitt = self.loadGlobalQuestVar("ubit")
   if npcId == KLEIN :
     if ubitt == "" :
       if st.getQuestItemsCount(STONE) >= 1:
         if underatak == "" :
           st.takeItems(STONE,1)
           st.getPlayer().teleToLocation(183920,-115544,-3294)
         else :
           return "<html><body><tr><td>Klein:</td></tr><br><font color=LEVEL>Valakas is under attack...</font><br>Try another time.</body></html>"
       else :
         return "<html><body><tr><td>Klein:</td></tr><br>You need <font color=LEVEL>Floating Stone</font> to enter...</body></html>"
     else : 
       return "<html><body><tr><td>Klein:</td></tr><br><font color=LEVEL>Valakas was killed...</font><br>Try another time.</body></html>"
   if npcId == HEART:
     access = self.loadGlobalQuestVar("access")
     if access == "":
       if ubitt == "" :   
         if underatak == "" :
           self.saveGlobalQuestVar("access", "1")
           self.startQuestTimer("prosnuca",1200000,npc,player) #1200000
           GrandBossManager.getInstance().getZone(204167,-111564,61).allowPlayerEntry(player, 30)
           st.getPlayer().teleToLocation(204167,-111564,61)
         else :
           return "<html><body>Valakas is under attack...<br>Try another time.</body></html>"
       else :
         return "<html><body>Valakas was killed...<br>Try another time.</body></html>"
     else :
       GrandBossManager.getInstance().getZone(204167,-111564,61).allowPlayerEntry(player, 30)
       st.getPlayer().teleToLocation(204167,-111564,61)
   return

 def onKill(self,npc,player,isPet):
   npcId = npc.getNpcId()
   if npcId == VALAKAS :
     self.addSpawn(31859,213001,-114890,-1635,0,False,900000)
     self.deleteGlobalQuestVar("lasthit")
     self.deleteGlobalQuestVar("underattack")
     respawnTime = long((192 + Rnd.get(144)) * 3600000)
     self.saveGlobalQuestVar("ubit", "1")
     self.saveGlobalQuestVar("respawn", str(System.currentTimeMillis() + respawnTime))
     self.startQuestTimer("resp", respawnTime, None, None)
     self.cancelQuestTimer("vkrovatku",npc,None)
     print "GrandBossManager:  Valakas was killed."
   elif npcId == ONE :
     DoorTable.getInstance().getDoor(24210004).openMe()
   elif npcId == TWOLEFT :
     DoorTable.getInstance().getDoor(24210006).openMe()
   elif npcId == TWORIGHT :
     DoorTable.getInstance().getDoor(24210005).openMe()
   return

QUEST = Valakas(-1,"valakas","grandbosses")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addStartNpc(KLEIN)
QUEST.addStartNpc(HEART)
QUEST.addTalkId(KLEIN)
QUEST.addTalkId(HEART)
QUEST.addKillId(VALAKAS)
QUEST.addKillId(ONE)
QUEST.addKillId(TWOLEFT)
QUEST.addKillId(TWORIGHT)
QUEST.addAttackId(VALAKAS)
