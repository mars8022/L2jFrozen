import sys
from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.network.serverpackets import CreatureSay
from com.l2jfrozen.util.random import Rnd

POLLEN = 6391
SKILL_NECTAR = 9999

# ����������
WATERED_SQUASH = [12774,12775,12776,12777,12778,12779]

class squash(JQuest) :

 def __init__(self,id,name,descr):
    JQuest.__init__(self,id,name,descr)
    # ��������
    self.adultSmallSquash = [12775,12776]
    self.adultLargeSquash = [12778,12779]

 def onAdvEvent(self,event,npc,player) :
    objId = npc.getObjectId()
    if event == "Good By" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"Good By!!  LOL."))
      npc.onDecay()
    elif event == "Good By1" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"����, �� ��������... ������� ����� ������� �� �������� ..."))
      npc.onDecay()
    elif event == "Good By2" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"�� ������ �������? ����� 30 ������ � ����� ..."))
    elif event == "Good By3" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"� ������ ��������� � ���� ����� 20 ������!"))
    elif event == "Good By4" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"� ���� �������� ����� 10 ������! 9. 8. 7 ..!"))
    elif event == "Good By5" and npc and player :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"��! ��������� ����������! �����, ������ ��� ���!"))
    return

 def onSkillUse(self,npc,player,skill):
    npcId = npc.getNpcId()
    skillId = skill.getId()
    if skillId != SKILL_NECTAR : return
    if npcId not in WATERED_SQUASH : return
    objectId = npc.getObjectId()
    if skillId == SKILL_NECTAR :
      # ������ �����
      if npc.getNectar() == 0 :
        if Rnd.get(2) == 1 :
          mytext = ["����� ���� ��������� �����, � ������ ���� ������ ������ ... ������ ����",
                    "���� �� ����� ������� �������� ��� ������ - � ������� �������!",
                    "��, ������ ���, ��������� ������! � ���� ������� ������������ � ������� �����!!!",
                    "������� ������, ����� ��������� �����!",
                    "���� ���������� ������� ����� �������� ��������, ����� ���� ������� �����! � ����� ������� ����� �������� ����� �������� � �������!",
                    "�, ����� �� ��������?",
                    "�������� ������� ��� �������� ���������?",
                    "�������! ��� - �����! ������?",
                    "����������! ������� 5 �������, ����� � ������ ������������ � ������� �����! �!"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
          npc.addGood()
        else :
          mytext = ["�� �����! ������� �����, � �� �������!",
                    "� �� �� �������, ���� ����������������� �� ������",
                    "�� ���� �� �� ����������! ������� �����, � �� �������!",
                    "���, ����� ������� ������",
                    "����� ����� �����������, �� �����, �������� ������� ������� � �������� �� �����!",
                    "� ���� ��� ������� ��������? ��������� �����"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
      # ������ �����
      elif npc.getNectar() == 1 :
        if Rnd.get(2) == 1 :
          mytext = ["����� ����� ������� ������!",
                    "���, ���, ���! �����! ��������� - ������!",
                    "��� ������, � ������ ��� ������?",
                    "������ - ������ ������! ��! ��! ��!"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
          npc.addGood()
        else :
          mytext = ["�! ����� ����! ����� ������� ������ ��������� ������?",
                    "���� � ���� ����� ��� ������, �� �������� ������ ������� ����� ...",
                    "���������� ������� �������! ������� ���� �� ����� ������� ������, ������� ����� �� ������!",
                    "T���� ��������� ����� �� ��� ������ ����? ��������� ������, � ���� ���� ������!"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
      # ������ �����
      elif npc.getNectar() == 2 :
        if Rnd.get(2) == 1 :
          mytext = ["T����, ������������! ������ ������� �����!",
                    "�� �������-�� ..., ��� ������������� ������! ���� ���?",
                    "���������� �� ���� ������ ��� ����, ����� ����? �������, �������� ��������� ��� ..., ����� �� ���� ����� �� ������������"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
          npc.addGood()
        else :
          mytext = ["�� ���� �� �� ����������? ����� ����?",
                    "������, ������� ����... � �� ���� ������� �������, � ������ ������� ..."]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
      # ��������� �����
      elif npc.getNectar() == 3 :
        if Rnd.get(2) == 1 :
          mytext = ["����� ������, ������� ����������� ������! ������ ��� ��������� ����� ������ ������?",
                    "���� �� �������� ����, � ��� ��� 10 ��������� adena!!! ��������?"]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
          npc.addGood()
        else :
          mytext = ["� �������, T� ����� ���� � �������?",
                    "T����� ������, ����� ����� ������� �������."]
          npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
          npc.addNectar()
      # ����� �����
      elif npc.getNectar() == 4 :
        if Rnd.get(2) == 1 :
          npc.addGood()
        if npc.getGood() >= 3 :
            if npcId == 12774 :
              newGourd = self.addSpawn(12775,npc)
              newGourd.setOwner(player.getName())
              self.startQuestTimer("Good By", 120000, newGourd, player)   # ����� 2 ������ ������������
              self.startQuestTimer("Good By2", 90000, newGourd, player)   # 30 ������ �� ������������
              self.startQuestTimer("Good By3", 100000, newGourd, player)  # 20 ������ �� ������������
              self.startQuestTimer("Good By4", 110000, newGourd, player)  # 10 ������ �� ������������
              mytext = ["������� �����, ��������! ���, ��� �������?",
                        "� ����� ����� 2 ������"]
              npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
              npc.onDecay()
            else :
              newGourd = self.addSpawn(12778,npc)
              newGourd.setOwner(player.getName())
              self.startQuestTimer("Good By1", 120000, newGourd, player)  # ����� 2 ������ ������������
              self.startQuestTimer("Good By2", 90000, newGourd, player)   # 30 ������ �� ������������
              self.startQuestTimer("Good By3", 100000, newGourd, player)  # 20 ������ �� ������������
              self.startQuestTimer("Good By4", 110000, newGourd, player)  # 10 ������ �� ������������
              mytext = ["������������� �������� ����� ������� ������. T����� ����������, � �������� ���� ��� ����� ������",
                        "� ����� ����� 2 ������"]          
              npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
              npc.onDecay()
        else :
          if npcId == 12774 :
            newGourd = self.addSpawn(12776,npc)
            newGourd.setOwner(player.getName())
            mytext = ["��! ���� - �� ����! ����! ������ ��! T� �� ������ ������� ������� ����������? � �� ��� �����!",
                      "������ ����, ���������? �� ��� ���� �����������",
                      "����� ������� � ...",
                      "�� ������ ������� �����? �� � ���� �������� ��������� �������� ..."]
            npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
            npc.onDecay()
          if npcId == 12777 :
            newGourd = self.addSpawn(12779,npc)
            newGourd.setOwner(player.getName())
            mytext = ["��! ���� - �� ����! ����! ������ ��! T� �� ������ ������� ������� ����������? � ��� �����!",
                      "������ ����, ���������? �� ��� ���� �����������",
                      "����� ������� � ...",
                      "�� ������ ������� �����? �� � ���� �������� ��������� �������� ..."]
            npc.broadcastPacket(CreatureSay(objectId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
            npc.onDecay()
    return

 def onAttack(self,npc,player,damage,isPet) :
    npcId = npc.getNpcId()
    objId = npc.getObjectId()
    if npcId not in WATERED_SQUASH : return
    if npcId  in self.adultLargeSquash :
      if Rnd.get(30) < 2 :
        mytext = ["����� ������ ������� ������ ..., ����� �������� ... ���� ...!",
                  "�� ��, �����! ��������� �� ����!",
                  "�� ������ ����� ��� �����������? �������� ���, ����� �� ������� ...",
                  "� ������ ���� �����! �, ���������� ���� �����!",
                  "�� ������� ������� ���� �����!",
                  "��, ���� ���� ������������� ������� �������?",
                  "� ��������� ���� �����, ����� �����!",
                  "�����, ����� ������� �����! ����� ��� �����!",
                  "T����� �������� ������ ����� ������� ������� �����... ���� ������ ������� � �������!"]
        npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),mytext[Rnd.get(len(mytext))]))
    return

 def onKill(self,npc,player,isPet) :
    npcId = npc.getNpcId()
    objId = npc.getObjectId()
    if npcId not in WATERED_SQUASH : return
    if npcId in self.adultSmallSquash :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"T���� �����������!!"))
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"����! �����������! ����� ������� �����  ..."))
    elif npcId in self.adultLargeSquash :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"T���� �����������!!"))
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"����! �����������! ����� ������� �����  ..."))
    else :
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"�� ���, ������?!"))
      npc.broadcastPacket(CreatureSay(objId,0,npc.getName(),"��, ����� ����������!!"))
    return

QUEST = squash(-1,"group_template","ai")

CREATED = State('Start', QUEST)
QUEST.setInitialState(CREATED)

for i in WATERED_SQUASH:
    QUEST.addSkillUseId(i)
    QUEST.addAttackId(i)
    QUEST.addKillId(i)