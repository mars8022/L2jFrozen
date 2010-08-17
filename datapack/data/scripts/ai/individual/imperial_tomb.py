from java.lang import System

from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.datatables.csv import DoorTable
from com.l2jfrozen.gameserver.managers import GrandBossManager
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.gameserver.network.serverpackets import CreatureSay
from com.l2jfrozen.gameserver.network.serverpackets import PlaySound
from com.l2jfrozen.gameserver.network.serverpackets import SocialAction
from com.l2jfrozen.gameserver.network.serverpackets import SpecialCamera
from com.l2jfrozen.util.random import Rnd

# Halisha and Frintezza
Halisha1 = 100200
Halisha1a = 29046
Halisha3 = 29047
FRINTEZZA = 29045
Ghost1 = 29050
Ghost2 = 29051
Ghost3 = 29050
Ghost4 = 29051

#other NPC
Messenger = 100110
FRINTEZZA_TELE = 32011

#MOBS SECOND ROOM SPAWN LOCATIONS
XXX = 174058
YYY = -81690
ZZZ = -5124

#OUTER MOBS SPAWN COORDS ROOM 2
XX1 = 175174
YY1 = -81804
ZZ1 = -5108
XX2 = 172988
YY2 = -81719
ZZ2 = -5108

#MOBS SPAWN LOCATIONS ROOM 1
XX = 174157
YY = -76232
ZZ = -5108

#SPAWN COORDS FOR FRINTEZZA AND HALISHA
FX = 174239
FY = -89802
FZ = -5021
HX = 174231
HY = -88006
HZ = -5115

#TELEPORT ITEM
FRINTEZZA_SCROLL = 8073

#STATUS
DORMANT = 0
FIGHTING = 2
DEAD = 3 

#I used a different statue than on official because the
#one used on retail doesnt show a HP bar on L2J and because
#this one is honestly cooler
STATUE_1 = 100100
STATUE_2 = 100101
STATUE_3 = 100102
STATUE_4 = 100103
STATUE_5 = 100104

class frintezza(JQuest):

    # init function. Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        # finally, don't forget to call the parent constructor to prepare the event TriggerThreeing
        # mechanisms etc.
        self.Despawn ={29045: {"bosses": [ 29045, 29046, 29047, 18335, 18338, 18334, 18334, 18329, 18330, 18331, 18332, 18333, 18329, 18336, 18337 ]},}
        self.MobSpawns ={
                18336: {"spawns": [ 18334, 18334, 18334, 18334 ], "chance": 30}, # mobs who will spawn
                18337: {"spawns": [ 18334, 18334, 18334, 18334 ], "chance": 30},
                29046: {},
                29047: {},
                31453: {},
                100200: {},
                100100: {"spawns": [ 18329, 18331, 18330, 18332, 18333 ], "room2": [ 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337 ], "chance": 30}, # mobs who will spawn
                100101: {"spawns": [ 18329, 18330, 18330, 18330, 18330 ], "room2": [ 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337 ], "chance": 30},
                100102: {"spawns": [ 18329, 18331, 18331, 18331, 18331 ], "room2": [ 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337 ], "chance": 30},
                100103: {"spawns": [ 18329, 18332, 18332, 18332, 18332 ], "room2": [ 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337 ], "chance": 30},
                100104: {"spawns": [ 18329, 18333, 18333, 18333, 18333 ], "room2": [ 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337, 18336, 18337 ], "chance": 30},
                }
        self.Visitors = []
        self.lastAttackVsHalisha = 0
        self.TriggerOne = True
        self.TriggerTwo = True
        self.TriggerThree = True
        self.isMorphed = False
        self.EntryLocked = False
        self.halisha1 = []
        self.halisha2 = []
        self.halisha3 = []
        self.SpawnGhost1 = []
        self.SpawnGhost2 = []
        self.SpawnGhost3 = []
        self.SpawnGhost4 = []
        self.frintezza = []
        self.FrintezzaZone = GrandBossManager.getInstance().getZone(174231,-88006,-5108)
        self.TombZone = GrandBossManager.getInstance().getZone(174157,-76232,-5108)
        info = GrandBossManager.getInstance().getStatsSet(FRINTEZZA)
        status = GrandBossManager.getInstance().getBossStatus(FRINTEZZA)
        if status == DEAD :
            # load the unlock date and time for Frintezza from DB
            temp = long(info.getLong("respawn_time")) - System.currentTimeMillis()
            if temp > 0 :
                self.EntryLocked = True
                self.startQuestTimer("FRINTEZZA_unlock", temp, None, None)
            else :
                # the time has already expired while the server was offline.
                # Set teleport to locked
                self.EntryLocked = False
                GrandBossManager.getInstance().setBossStatus(FRINTEZZA,DORMANT)
        else :
            self.EntryLocked = False

    def onTalk (self,npc,player) :
        npcId = npc.getNpcId()
        if npcId == FRINTEZZA_TELE :
            #need to check here if FRINTEZZA is dead, not done yet
            #data will be saved in quest global data
            if player.getQuestState("frintezza").getQuestItemsCount(FRINTEZZA_SCROLL) >= 1 :
                if self.EntryLocked == False :
                    party = player.getParty()
                    if party :
                        if party.isInCommandChannel() :
                            channel = party.getCommandChannel()
                            channelsize = len(channel.getPartys())
                            channelmembers = list(channel.getMembers())
                            channelleader = channel.getChannelLeader()
                            if channelsize >= 4 and channelsize <= 5 and channelleader == player:
                                player.getQuestState("frintezza").takeItems(FRINTEZZA_SCROLL,1)
                                self.TriggerOne = False
                                self.Visitors = channelmembers
                                pointA = []
                                for i in range(len(channelmembers)/2) :
                                    pointA.append(channelmembers.pop(Rnd.get(len(channelmembers))))
                                pointB = channelmembers
                                for i in pointA:
                                    if i :
                                        GrandBossManager.getInstance().getZone(174157,-76232,-5108).allowPlayerEntry(i, 60)
                                        i.teleToLocation(174157,-76232,-5108)#tele to location A
                                for i in pointB:
                                    if i :
                                        GrandBossManager.getInstance().getZone(174157,-76232,-5108).allowPlayerEntry(i, 60)
                                        i.teleToLocation(173102,-75248,-5108)#tele to location B
                                self.startQuestTimer("teleport_out", 2100000, None, None)
                                self.startQuestTimer("Area_open", 10800000, None, None)
                                self.EntryLocked = True
                                return "<html><body>Your group has been split into 2.  The group on the outer rim need to kill the alarms in order to free the one inside.  From this point, you have 35 minutes to complete the challenges and get into Frintezza's chambers</body></html>"
                            else:
                                return "<html><body>Your command channel needs to have at least 4 parties and a maximum of 5.</html></body>"
                        else:
                            return "<html><body>You are not in a command channel</html></body>"
                    else:
                        return "<html><body>You are not in a party</html></body>"
                else:
                    return "<html><body>Either there is another group in the Last Imperial Tomb or Frintezza cannot recieve visitors at the moment. Come back later.</html></body>"
            else:
                return "<html><body>You do not have the necessary scroll in your possession.</html></body>"
        return

    def onAttack (self,npc,player,damage,isPet) :
        npcId = npc.getNpcId()
        objId = npc.getObjectId()
        if npcId == STATUE_1 or npcId == STATUE_2 or npcId == STATUE_3 or npcId == STATUE_4 or npcId == STATUE_5 :
            if self.EntryLocked == False :
                #Makes sure that no other group goes to the Last Imperial Tomb when another group is there
                self.EntryLocked = True
        if npcId == FRINTEZZA :
            self.frintezza.setCurrentHpMp(790857,10000)
        if npcId == Halisha1 :
            self.lastAttackVsHalisha = System.currentTimeMillis()
            self.startQuestTimer("FRINTEZZA_despawn",60000, None, None)
            if self.TriggerThree == False :
                self.TriggerThree = True
            if self.TriggerThree == True :
                CurrentHP = npc.getCurrentHp()
                if CurrentHP <= npc.getMaxHp() * 0.5 and self.isMorphed == False:
                    self.startQuestTimer("poly1", 100, npc, player)
                    self.isMorphed = True
        if npcId == Halisha1a :
            self.lastAttackVsHalisha = System.currentTimeMillis()
            self.startQuestTimer("FRINTEZZA_despawn",60000, None, None)
        if npcId == Halisha3 :
            self.lastAttackVsHalisha = System.currentTimeMillis()
            self.startQuestTimer("FRINTEZZA_despawn",60000, None, None)
        return

    def onKill (self,npc,player,isPet) :
        npcId = npc.getNpcId()
        npcObjId = npc.getObjectId()
        rr2 = Rnd.get(700)
        rr = Rnd.get(690)
        MobSpawns = self.MobSpawns
        if MobSpawns.has_key(npcId) :
            if npcId == 100100 or npcId == 100101 or npcId == 100102 or npcId == 100103 or npcId == 100104 :
                if self.TriggerOne == False :
                    if Rnd.get(100) < MobSpawns[npcId]["chance"] :
                        #First sequence finished, open the walls and doors and spawn the mobs in the second room
                        self.TriggerOne = True
                        self.TriggerTwo = False
                        for i in range(len(MobSpawns[npcId]["spawns"])):
                            rr2 = Rnd.get(500)
                            rr = Rnd.get(480)
                            objId = self.addSpawn(MobSpawns[npcId]["spawns"][i],XX+(250-rr),YY+(250-rr2),ZZ,0,False,0)
                        for i in range(25150051,25150059):
                            DoorTable.getInstance().getDoor(i).openMe()
                        DoorTable.getInstance().getDoor(25150042).openMe()
                        DoorTable.getInstance().getDoor(25150043).openMe()
                        for i in range(len(MobSpawns[npcId]["room2"])):
                            rr2 = Rnd.get(680)
                            rr = Rnd.get(700)
                            objId = self.addSpawn(MobSpawns[npcId]["room2"][i],XXX+(350-rr),YYY+(350-rr2),ZZZ,0,False,0)
                    else :
                        for i in range(len(MobSpawns[npcId]["spawns"])):
                            rr2 = Rnd.get(700)
                            rr = Rnd.get(680)
                            objId = self.addSpawn(MobSpawns[npcId]["spawns"][i],XX+(350-rr),YY+(350-rr2),ZZ,0,False,0)
            if npcId == 18336 or npcId == 18337 :
                if Rnd.get(100) < MobSpawns[npcId]["chance"] :
                    if self.TriggerTwo == False :
                        self.TriggerTwo = True
                        if player:
                            for play in self.Visitors :
                                if play :
                                    GrandBossManager.getInstance().getZone(174231,-88006,-5115).allowPlayerEntry(play, 360)
                        for i in range(25150061,25150071):
                            DoorTable.getInstance().getDoor(i).openMe()
                        DoorTable.getInstance().getDoor(25150045).openMe()
                        DoorTable.getInstance().getDoor(25150046).openMe()
                        for i in range(25150051,25150059):
                            DoorTable.getInstance().getDoor(i).closeMe()
                        DoorTable.getInstance().getDoor(25150042).closeMe()
                        DoorTable.getInstance().getDoor(25150043).closeMe()
                        for i in range(len(MobSpawns[npcId]["spawns"])):
                            objId = self.addSpawn(MobSpawns[npcId]["spawns"][i],XX1,YY1,ZZ1,0,False,0)
                        for i in range(len(MobSpawns[npcId]["spawns"])):
                            objId = self.addSpawn(MobSpawns[npcId]["spawns"][i],XX2,YY2,ZZ2,0,False,0)
                        self.startQuestTimer("halisha_timer", 210000, npc, player)
                        self.frintezza = self.addSpawn(FRINTEZZA,FX,FY,FZ,0,False,0)
                        GrandBossManager.getInstance().addBoss(self.frintezza)
            if npcId == Halisha1a :
                self.startQuestTimer("poly2", 7500, npc, player)
                npc.setRHandId(8204)
                self.isMorphed = False
            if npcId == Halisha3 :
                respawnTime = long(48 * 3600000)
                npc.broadcastPacket(PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()))
                self.startQuestTimer("FRINTEZZA_unlock", 432000000, None, None)
                self.startQuestTimer("spawn_cubes", 10000, npc, None)
                if self.frintezza:
                    self.frintezza.onDecay()
                GrandBossManager.getInstance().setBossStatus(FRINTEZZA,DEAD)
                # also save the respawn time so that the info is maintained past reboots
                info = GrandBossManager.getInstance().getStatsSet(FRINTEZZA)
                info.set("respawn_time",(long(System.currentTimeMillis()) + respawnTime))
                GrandBossManager.getInstance().setStatsSet(FRINTEZZA,info)
                #I set the unlock for 5 days but this value needs to be checked
        return

    def onAdvEvent (self,event,npc,player) :
        if event == "poly1" and npc and player:
            heading = npc.getHeading()
            hp = npc.getCurrentHp()
            mp = npc.getCurrentMp()
            npc.deleteMe()
            self.halisha2 = self.addSpawn(Halisha1a, npc)
            self.halisha2.broadcastPacket(CreatureSay(self.halisha2.getObjectId(),0,"Scarlet Von Halisha","You will die!!!"))
            self.startQuestTimer("poly1a", 100, npc, player)
            self.halisha2.setHeading(heading)
            self.halisha2.setCurrentHpMp(hp,mp)
            self.cancelQuestTimer("poly1", npc, None)
        elif event == "poly1a" and npc and player:
            self.halisha2.broadcastPacket(SpecialCamera(self.halisha2.getObjectId(),800,0,150,0,7000))
            self.halisha2.broadcastPacket(SocialAction(self.halisha2.getObjectId(),1))
            self.startQuestTimer("attack", 7000, npc, player)
        elif event == "attack" and npc and player:
            self.halisha2.addDamageHate(player,0,500)
            self.halisha2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
        elif event == "poly2" and npc and player:
            self.halisha3 = self.addSpawn(Halisha3, npc)
            self.halisha3.addDamageHate(player,0,500)
            self.halisha3.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
            self.cancelQuestTimer("poly2", npc, None)
        elif event == "halisha_timer":
            self.halisha1 = self.addSpawn(Halisha1,HX,HY,HZ,0,False,0)
            self.frintezza.broadcastPacket(CreatureSay(self.frintezza.getObjectId(),0,"Frintezza","Halisha!!! Get rid of these ones!"))
            self.frintezza.broadcastPacket(SpecialCamera(self.frintezza.getObjectId(),200,-50,150,0,6000))
            self.frintezza.broadcastPacket(SocialAction(self.frintezza.getObjectId(),3))
            self.SpawnGhost1 = self.addSpawn(Ghost1,175840,-87156,-5089,21221,False,0)
            self.SpawnGhost2 = self.addSpawn(Ghost2,175863,-88702,-5108,29413,False,0)
            self.SpawnGhost3 = self.addSpawn(Ghost3,172618,-88696,-5108,4962,False,0)
            self.SpawnGhost4 = self.addSpawn(Ghost4,172643,-87179,-5108,62044,False,0)
            self.cancelQuestTimer("halisha_timer", npc, None)
        elif event == "FRINTEZZA_unlock" :
            GrandBossManager.getInstance().addBoss(FRINTEZZA)
            GrandBossManager.getInstance().setBossStatus(FRINTEZZA,DORMANT)
            self.cancelQuestTimer("FRINTEZZA_unlock", npc, None)
            self.EntryLocked = False
        elif event == "spawn_cubes" :
            cube = self.addSpawn(31859,HX,HY,HZ,0,False,0)
            self.cancelQuestTimer("FRINTEZZA_despawn", npc, None)
            self.cancelQuestTimer("spawn_cubes", npc, None)
            self.startQuestTimer("remove_players",900000, None, None)
        elif event == "remove_players" :
            self.FrintezzaZone.oustAllPlayers()
        elif event == "teleport_out" :
            # Now we need to send back to town the players who did not make
            # it to Frintezza's chamber within 35minutes(like retail)
            for i in range(25150061,25150071):
                DoorTable.getInstance().getDoor(i).closeMe()
            DoorTable.getInstance().getDoor(25150045).closeMe()
            DoorTable.getInstance().getDoor(25150046).closeMe()
            self.cancelQuestTimer("teleport_out", npc, None)
            self.TombZone.oustAllPlayers()
        elif event == "Area_open" :
            if GrandBossManager.getInstance().getBossStatus(FRINTEZZA) != DEAD and self.lastAttackVsHalisha == 0 :
                self.FrintezzaZone.oustAllPlayers()
                self.EntryLocked = False
        elif event == "FRINTEZZA_despawn" and npc:
            if (self.lastAttackVsHalisha + 1800000 < System.currentTimeMillis()) :
                if self.SpawnGhost1:
                    self.SpawnGhost1.deleteMe()
                if self.SpawnGhost2:
                    self.SpawnGhost2.deleteMe()
                if self.SpawnGhost3:
                    self.SpawnGhost3.deleteMe()
                if self.SpawnGhost4:
                    self.SpawnGhost4.deleteMe()
                if self.halisha1:
                    self.halisha1.deleteMe()
                if self.halisha2:
                    self.halisha2.deleteMe()
                if self.halisha3:
                    self.halisha3.deleteMe()
                if self.frintezza:
                    self.frintezza.deleteMe()
                self.lastAttackVsHalisha = 0
                self.EntryLocked = False
                self.TombZone.oustAllPlayers()
                self.FrintezzaZone.oustAllPlayers()
                self.cancelQuestTimer("FRINTEZZA_despawn", npc, None)
        return

# now call the constructor (starts up the ai)
QUEST = frintezza(-1,"frintezza","ai")
CREATED = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)

for i in QUEST.MobSpawns.keys() :
    QUEST.addAttackId(i)
    QUEST.addKillId(i)
    QUEST.addTalkId(i)

QUEST.addStartNpc(FRINTEZZA_TELE)
