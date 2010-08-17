import sys
from interlude.util import Rnd
from interlude import Config
from interlude.gameserver.ai import CtrlIntention
from interlude.gameserver.model.quest import State
from interlude.gameserver.model.quest import QuestState
from interlude.gameserver.model.quest.jython import QuestJython as JQuest
from interlude.gameserver.network.serverpackets import NpcSay

MutationId ={   # Npc:[NewNpc,% for chance by shot,Type Message,nb message]
                # if Quest_Drop = 5 => 5% by shot to change mob
                # 21261:[21262,1,"Type1",5],      # 1st mutation Ol Mahum Transcender
                # 21262:[21263,1,"Type1",5],      # 2st mutation Ol Mahum Transcender
                # 21263:[21264,1,"Type1",5],      # 3rd mutation Ol Mahum Transcender
                # 21258:[21259,100,"Type1",5],    # always mutation on atk Fallen Orc Shaman
                20835:[21608,1,"Type1",5],      # zaken's seer to zaken's watchman
                21608:[21609,1,"Type1",5],      # zaken's watchman
                20832:[21602,1,"Type1",5],      # Zaken's pikeman
                21602:[21603,1,"Type1",5],      # Zaken's pikeman
                20833:[21605,1,"Type1",5],      # Zaken's archet
                21605:[21606,1,"Type1",5],      # Zaken's archet
                21625:[21623,1,"Type1",5],      # zaken's Elite Guard to zaken's Guard
                21623:[21624,1,"Type1",5],      # zaken's Guard
                20842:[21620,1,"Type1",5],      # Musveren
                21620:[21621,1,"Type1",5]       # Musveren
                }
# message by Name on MutationId
NpcMessage={   "Type1":["I cannot despise the fellow! I see his sincerity in the duel.",
                              "Nows we truly begin!",
                              "Fool! Right now is only practice!",
                              "Have a look at my true strength.",
                              "This time at the last! The end!"],
               "Type2":[" "] # posibility to add other message on type 2 and change on MutationId
            }

# Main Quest Code
class mutation(JQuest):
   
  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
  
  def onAttack(self,npc,player,damage,isPet):
    npcId = npc.getNpcId()
    NewMob,chance,NameNpc,NbMessage = MutationId[npcId]
    if Rnd.get(100) <= chance*Config.RATE_DROP_QUEST :
       if MutationId.has_key(npcId) :
          Msg=NpcMessage[NameNpc]
          npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),Msg[Rnd.get(NbMessage-1)]))
          npc.deleteMe()
          newNpc = self.addSpawn(NewMob,npc,)
          newNpc.addDamageHate(player,0,99999)
          newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
    return

QUEST       = mutation(-1,"mutation","ai")

for i in MutationId.keys() :
   QUEST.addAttackId(i)