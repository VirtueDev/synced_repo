//
// $Id$

package tutorial {

public class Quest
{
    public var questId :String;
    public var trigger :String;
    public var status :String;
    public var intro :String;
    public var outro :String;
    public var summary :String;
    public var payout :int;

    public function Quest (questId :String, trigger :String, status :String, intro :String,
                           summary :String, outro :String, payout :uint)
    {
        this.questId = questId;
        this.trigger = trigger;
        this.status = status;
        this.intro = intro;
        this.summary = summary;
        this.outro = outro;
        this.payout = payout;
    }

    public static function getQuestCount () :uint
    {
        fillQuests();
        return _quests.length;
    }

    public static function getQuest (step :uint) :Quest
    {
        fillQuests();
        return _quests[step];
    }

    protected static function fillQuests () :void
    {
        if (_quests) {
            return;
        }
        _quests = new Array();
        _quests.push(new Quest(
            "editProfile",
            "profileEdited",
            "Edit your Profile",
            "Fill out your profile and receive 500 flow.",
            "Choose Me -> My Profile to see your Whirled Profile page.<br>" +
            "Click Edit to make changes.",
            "Congratulations! You updated your profile and received 500 flow.",
            500));
        _quests.push(new Quest(
            "buyDecor",
            "decorBought",
            "Buy new Decor",
            "Your room's background image is known as decor. Let's go shopping.",
            "<p align=\"center\"><font size=\"24\"><b>Change Your Decor!</b></font></p>" +
            "<p align=\"center\"><font face=\"Arial\" size=\"16\"><b>" +
            "The decor is the most fundamental element of your room's appearance. Every other item in your room appears on top the decor." +
            "</b></font></p>" +
            "<p align=\"left\"><font face=\"Arial\" size=\"14\">" +
            "1. Choose <b><i>Catalog -> Decor</i></b> for a selection of new room settings.<br><br>" +
            "2. Browse through and buy one you like.<br><br>" +
            "3. This is a continuation of the previous paragraph because I want to see some overflow.",
            "Good. You now own a piece of decor.",
            0));
        _quests.push(new Quest(
            "installDecor",
            "decorInstalled",
            "Change Decor",
            "Now we need to install your new decor.",
            "Choose My Stuff -> Decor to see the decor you own.<br>" +
            "Apply your new decor by clicking the 'Add to Room' button.<br>" +
            "Click the close box to return to your room.",
            "Congratulations! You received 200 flow for changing your decor.",
            200));
        _quests.push(new Quest(
            "buyFurni",
            "furniBought",
            "Buy Furniture",
            "Furniture adds depth and personality to a room. Let's shop some more.",
            "Choose Catalog -> Furniture to find something you like.",
            "We now have furniture to install in our room.",
            0));
        _quests.push(new Quest(
            "installFurni",
            "furniInstalled",
            "Install your furniture",
            "The furniture won't show up until you add it to your room.<br><br>We'll do that next.",
            "Choose My Stuff -> Furniture to browse your furniture. Clicking 'Add to Room' will place the item in the center of your room.",
            "Excellent. You received 150 flow for adding furniture to your room.",
            300));
        _quests.push(new Quest(
            "placeFurni",
            "editorClosed",
            "Place your furniture",
            "The new furniture appears in the middle of the room until you drag it to where you want it to be.",
            "Click and drag your furni to place it.  Click the Close box on the Room Editing dialog box to return to your room.",
            "Congratulations! You received 150 flow for adjusting the furniture's position.",
            300));
        _quests.push(new Quest(
            "buyAvatar",
            "avatarBought",
            "Buy a new Avatar",
            "Find a new face. There's lots to choose from in the catalog.",
            "Click on Catalog -> Avatars to browse the selection. Purchase one you like.",
            "Great. We're ready to switch into your new avatar.",
            0));
        _quests.push(new Quest(
            "wearAvatar",
            "avatarInstalled",
            "Wear your new Avatar",
            "Just as with decor and furni, your new item won't show in the world until you add it.",
            "Choose My Stuff -> Avatars to view your avatars. Click the \"Wear Avatar\" button to change your avatar.",
            "Congratulations! You received 200 flow for changing your avatar.<br><br>" +
            "This concludes the tutorial. Unfortunately we don't yet know how to turn ourselves off, so you will need to click on the little 'X' to leave us. Good luck out there!<br><br>",
            200));
    }

    protected static var _quests :Array;
}
}
