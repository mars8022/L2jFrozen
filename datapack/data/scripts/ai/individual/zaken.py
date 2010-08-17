import sys
from com.l2scoria.gameserver import GameTimeController
from com.l2scoria.gameserver.ai import CtrlIntention
from com.l2scoria.gameserver.datatables.csv import DoorTable
from com.l2scoria.gameserver.datatables import SkillTable
from com.l2scoria.gameserver.model.quest import State
from com.l2scoria.gameserver.model.quest import QuestState
from com.l2scoria.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2scoria.util.random import Rnd
from java.lang import System

ZAKEN = 29022

class zaken (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def init_LoadGlobalData(self) :
   underatak = self.loadGlobalQuestVar("underattack")
   ubitt = self.loadGlobalQuestVar("ubit")
   self.startQuestTimer("doorOpen", 30000, None, None)
   if ubitt == "1" :
     temp = long(self.loadGlobalQuestVar("respawn")) - System.currentTimeMillis()
     if temp > 0 :
       print "Zaken: dead"
       self.startQuestTimer("resp", temp, None, None)
       self.deleteGlobalQuestVar("underattack")
     else :
       print "Zaken: live"
       zakenn = self.addSpawn(ZAKEN,55256,219114,-3224,30000,False,0)
       self.startQuestTimer("tpchk",600000,zakenn,None)
       self.deleteGlobalQuestVar("underattack")
   elif underatak == "1" :
     print "Zaken: under attack"
     self.deleteGlobalQuestVar("underattack")
   else :
     print "Zaken: live"
     self.deleteGlobalQuestVar("ubit")
     self.deleteGlobalQuestVar("respawn")
     zakenn = self.addSpawn(ZAKEN,55256,219114,-3224,30000,False,0)
     self.startQuestTimer("tpchk",600000,zakenn,None)
     self.deleteGlobalQuestVar("underattack")
   return

 def onAdvEvent (self,event,npc,player):
   if event == "tpchk" :
     underatak = self.loadGlobalQuestVar("underattack")
     if underatak == "" :
       npc.setTarget(npc)
       npc.doCast(SkillTable.getInstance().getInfo(4222,1))
       self.startQuestTimer("tpchk",600000,npc,None)
     else:
       self.deleteGlobalQuestVar("underattack")
       self.startQuestTimer("tpchk",600000,npc,None)
   elif event == "resp" :
     self.deleteGlobalQuestVar("ubit")
     self.deleteGlobalQuestVar("respawn")
     zakenn = self.addSpawn(ZAKEN,55606,218755,-3251,30000,False,0)
     self.startQuestTimer("tpchk",600000,zakenn,None)
     self.deleteGlobalQuestVar("underattack")
     self.cancelQuestTimer("resp",npc,None)
   elif event == "doorOpen" :
     time = GameTimeController.getInstance().getGameTime()
     hour = (time/60)%24
     if hour == 0 :
       DoorTable.getInstance().getDoor(21240006).openMe();
       self.startQuestTimer("doorOpen",1800000,None,None)
       self.startQuestTimer("doorClose",300000,None,None)
     else :
       self.startQuestTimer("doorOpen",30000,None,None)
   elif event == "doorClose" :
     DoorTable.getInstance().getDoor(21240006).closeMe();
     time = GameTimeController.getInstance().getGameTime()
   return

 def onAttack (self,npc,player,damage,isPet):
   self.saveGlobalQuestVar("underattack", "1")
   maxHp = npc.getMaxHp()
   nowHp = npc.getCurrentHp()
   if npc.getCurrentMp() > 50 :
     if (Rnd.get(15) < 1):
       ch = Rnd.get(15*15)
       if ch < 1:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4216,1))
       elif ch < 2:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4217,1))
       elif ch < 4:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4219,1))
       elif ch < 8:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4218,1))
       else:
         if ch < 15:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4221,1))
       if Rnd.get(2) < 1:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4220,1))
     elif (Rnd.get(10) < 1):
       ch = Rnd.get(15*15)
       if ch < 1:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4216,1))
       elif ch < 2:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4217,1))
       elif ch < 4:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4219,1))
       elif ch < 8:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4218,1))
       else:
         if ch < 15:
           npc.setTarget(player)
           npc.doCast(SkillTable.getInstance().getInfo(4221,1))
       if Rnd.get(2) < 1:
         npc.setTarget(player)
         npc.doCast(SkillTable.getInstance().getInfo(4220,1))
     else:
       if nowHp < maxHp*0.25:
         chh = (Rnd.get(20))
         if chh < 1:
           npc.setTarget(npc)
           npc.doCast(SkillTable.getInstance().getInfo(4222,1))
   return

 def onKill(self,npc,player,isPet):
   self.deleteGlobalQuestVar("underattack")
   self.cancelQuestTimer("tpchk",npc,None)
   respawnTime = long((32 + Rnd.get(16)) * 3600000)
   self.saveGlobalQuestVar("ubit", "1")
   self.saveGlobalQuestVar("respawn", str(System.currentTimeMillis() + respawnTime))
   self.startQuestTimer("resp", respawnTime, None, None)
   print "GrandBossManager:  Zaken was killed."
   return

QUEST = zaken(-1, "zaken", "grandbosses")
CREATED = State('Start', QUEST)

QUEST.setInitialState(CREATED)

QUEST.addAttackId(ZAKEN)
QUEST.addKillId(ZAKEN)