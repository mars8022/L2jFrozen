import sys
from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.datatables.sql import SpawnTable
from com.l2jfrozen.gameserver.model.actor.position import L2CharPosition
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.network.serverpackets import ActionFailed
from com.l2jfrozen.gameserver.network.serverpackets import PlaySound
from com.l2jfrozen.gameserver.network.serverpackets import SocialAction
from com.l2jfrozen.gameserver.network.serverpackets import SpecialCamera

Doctor_Chaos = 32033
Strange_Machine = 32032
Chaos_Golem = 25512

class Quest (JQuest) :
  def __init__(self,id,name,descr):
    JQuest.__init__(self,id,name,descr)
    self.isGolemSpawned = 0
    self.isChaosSpawned = 1

  def FindTemplate (self, npcId) :
    npcinstance = 0
    for spawn in SpawnTable.getInstance().getSpawnTable().values():
        if spawn :
            if spawn.getNpcid() == npcId:
                npcinstance = spawn.getLastSpawn()
                break
    return npcinstance

  def onAdvEvent (self,event,npc, player) :
    if event == "1" :
       machine_instance = self.FindTemplate(Strange_Machine)
       if machine_instance :
         npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, machine_instance)
         machine_instance.broadcastPacket(SpecialCamera(machine_instance.getObjectId(),1,-200,15,10000,20000))
       else :
         print "Dr Chaos AI: problem finding Strange Machine (npcid = "+Strange_Machine+"). Error: not spawned!"
       self.startQuestTimer("2",2000,npc,player)
       self.startQuestTimer("3",10000,npc,player)
       if self.isChaosSpawned == 0 :
          chaos = self.addSpawn(32033,96471,-111425,-3334,0,False,0)
          self.isChaosSpawned == 1
       self.startQuestTimer("2",2000,npc,player)
       self.startQuestTimer("3",10000,npc,player)
    elif event == "2" :
       npc.broadcastPacket(SocialAction(npc.getObjectId(),3))
    elif event == "3" :
       npc.broadcastPacket(SpecialCamera(npc.getObjectId(),1,-150,10,3000,20000))
       self.startQuestTimer("4",2500,npc,player)
    elif event == "4" :
       npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, L2CharPosition(96055,-110759,-3312,0))
       self.startQuestTimer("5",2000,npc,player)
    elif event == "5" :
       player.teleToLocation(94832,-112624,-3304)
       npc.teleToLocation(-113091,-243942,-15536)
       if self.isGolemSpawned == 0 :
          golem = self.addSpawn(25512,94640,-112496,-3336,0,False,0)
          self.isGolemSpawned == 1
          self.startQuestTimer("6",1000,golem,player)
          player.sendPacket(PlaySound(1,"Rm03_A",0,0,0,0,0))
    elif event == "6" :
       npc.broadcastPacket(SpecialCamera(npc.getObjectId(),30,-200,20,6000,8000))
    return

  def onFirstTalk (self,npc,player):
    npcId = npc.getNpcId()
    if npcId == Doctor_Chaos :
       npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, L2CharPosition(96323,-110914,-3328,0))
       self.startQuestTimer("1",3000,npc,player)
    return ""

QUEST      = Quest(-1,"Doctor Chaos","ai")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addFirstTalkId(32033)