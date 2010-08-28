import sys
from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.managers import GrandBossManager
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.util.random import Rnd
from java.lang import System

ORFEN = 29014

#Orfen status tracking
DEAD = 0
LIVE = 1

class orfen(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.FirstAttacked = False
        self.Teleported = False

    def init_LoadGlobalData(self) :
        info = GrandBossManager.getInstance().getStatsSet(ORFEN)
        status = GrandBossManager.getInstance().getBossStatus(ORFEN)
        if status == DEAD :
          temp = long(info.getLong("respawn_time")) - System.currentTimeMillis()
          if temp > 0 :
            print "Orfen: dead"
            self.startQuestTimer("orfen_spawn", temp, None, None)
          else :
            self.addSpawn(ORFEN,55024,17368,-5412,0,False,0)
            GrandBossManager.getInstance().setBossStatus(ORFEN,LIVE)
            print "Orfen: live"
        if status == LIVE :
          self.addSpawn(ORFEN,55024,17368,-5412,0,False,0)
          print "Orfen: live"
        return

    def onAdvEvent (self,event,npc,player):
        if event == "orfen_spawn" :
          GrandBossManager.getInstance().setBossStatus(ORFEN,LIVE)
          self.addSpawn(ORFEN,55024,17368,-5412,0,False,0)
        if event == "Refresh_status" :
          if npc.getCurrentHp() >= npc.getMaxHp()-5 :
            self.startQuestTimer("orfen_return", 1000, npc, None)
            self.cancelQuestTimer("Refresh_status",npc,None)
          self.startQuestTimer("Refresh_status", 10000, npc, None)
        if event == "orfen_return" :
          npc.teleToLocation(55024,17368,-5412, False)
          self.Teleported = False
          self.FirstAttacked = False
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE)
          npc.setCanReturnToSpawnPoint(True)
        return

    def onAttack (self,npc,player,damage,isPet):
        if self.FirstAttacked :
          if npc.getCurrentHp() < npc.getMaxHp() / 2 :
            if self.Teleported == False :
              npc.clearAggroList()
              npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE)
              npc.teleToLocation(43577,15985,-4396, False)
              self.Teleported = True
              npc.setCanReturnToSpawnPoint(False)
              self.startQuestTimer("Refresh_status", 10000, npc, None)
        else :
          self.FirstAttacked = True
        return

    def onKill(self,npc,player,isPet):
        self.FirstAttacked = False
        respawnTime = long(Config.ORFEN_RESP_FIRST + Rnd.get(Config.ORFEN_RESP_SECOND)) * 3600000
        GrandBossManager.getInstance().setBossStatus(ORFEN,DEAD)
        self.startQuestTimer("orfen_spawn", respawnTime, None, None)
        info = GrandBossManager.getInstance().getStatsSet(ORFEN)
        info.set("respawn_time",(long(System.currentTimeMillis()) + respawnTime))
        GrandBossManager.getInstance().setStatsSet(ORFEN,info)
        return 

# now call the constructor (starts up the ai)
QUEST = orfen(-1,"orfen","ai")
CREATED = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)

QUEST.addKillId(ORFEN)
QUEST.addAttackId(ORFEN)