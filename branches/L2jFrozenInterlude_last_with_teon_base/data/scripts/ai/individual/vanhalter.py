import sys
from interlude.gameserver.model.quest import State
from interlude.gameserver.model.quest import QuestState
from interlude.gameserver.model.quest.jython import QuestJython as JQuest
from interlude.gameserver.instancemanager import VanHalterManager

class vanhalter(JQuest):

  def __init__(self,id,name,descr) : JQuest.__init__(self,id,name,descr)


  def onAttack (self,npc,player,damage,isPet) :
    npcId = npc.getNpcId()
    if npcId == 29062 :
      maxHp = npc.getMaxHp()
      curHp = npc.getCurrentHp()
      if (curHp / maxHp) * 100 <= 20 :
        VanHalterManager.getInstance().callRoyalGuardHelper()

  def onKill (self,npc,player,isPet) :
    npcId = npc.getNpcId()
    if npcId == 32058 or npcId == 32059 or npcId == 32060 or npcId == 32061 or npcId == 32062 or npcId == 32063 or npcId == 32064 or npcId == 32065 or npcId == 32066 :
      VanHalterManager.getInstance().removeBleeding(npcId)
      VanHalterManager.getInstance().checkToriolRevelationDestroy()
    if npcId == 22188 :
      VanHalterManager.getInstance().checkRoyalGuardCaptainDestroy()
    if npcId == 29062 :
      VanHalterManager.getInstance().enterInterval()

QUEST       = vanhalter(-1,"vanhalter","ai")

QUEST.addAttackId(29062)
QUEST.addKillId(29062)
QUEST.addKillId(22188)
QUEST.addKillId(32058)
QUEST.addKillId(32059)
QUEST.addKillId(32060)
QUEST.addKillId(32061)
QUEST.addKillId(32062)
QUEST.addKillId(32063)
QUEST.addKillId(32064)
QUEST.addKillId(32065)
QUEST.addKillId(32066)
