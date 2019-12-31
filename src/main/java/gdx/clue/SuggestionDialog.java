package gdx.clue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.utils.Align;
import static gdx.clue.Card.*;
import java.util.ArrayList;
import java.util.List;

public class SuggestionDialog extends Window {

    public static int WIDTH = 300;
    public static int HEIGHT = 400;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final GameScreen screen;

    private final List<CardCheckBox> checkBoxes = new ArrayList<>();
    private final List<Card> suggestion = new ArrayList<>();

    public SuggestionDialog(final ShowCardsRoutine showCards, final GameScreen screen, final Player player, final Card roomCard) {
        super("Suggest a Murder Committed in the " + roomCard, ClueMain.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;

        setSkin(ClueMain.skin);
        setModal(true);
        defaults().pad(5);

        Table table = new Table();
        table.align(Align.left | Align.top).pad(10);
        table.columnDefaults(0).expandX().left().uniformX();
        table.columnDefaults(1).expandX().left().uniformX();
        table.columnDefaults(2).expandX().left().uniformX();

        ScrollPane sp = new ScrollPane(table, ClueMain.skin);
        add(sp).expand().fill().minWidth(200);
        row();

        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.setMaxCheckCount(1);
        buttonGroup1.setMinCheckCount(0);

        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.setMaxCheckCount(1);
        buttonGroup2.setMinCheckCount(0);

        table.add(new Label("blue = your cards in hand", ClueMain.skin, "default-blue"));
        table.row();
        table.add(new Label("red = already marked off in notebook", ClueMain.skin, "default-red"));
        table.row();

        table.add(new Label("Pick the Suspect", ClueMain.skin, "default-yellow"));
        table.row();
        for (int i = 0; i < NUM_SUSPECTS; i++) {
            Card card = new Card(TYPE_SUSPECT, i);
            CardCheckBox cb = new CardCheckBox(card, player.isCardInHand(card), player.getNotebook().isCardToggled(card));
            checkBoxes.add(cb);
            buttonGroup1.add(cb);
            table.add(cb);
            if ((i + 1) % 3 == 0) {
                table.row();
            }
        }

        table.row();
        table.add(new Label("", ClueMain.skin));
        table.row();

        table.add(new Label("Pick the Weapon", ClueMain.skin, "default-yellow"));
        table.row();
        for (int i = 0; i < NUM_WEAPONS; i++) {
            Card card = new Card(TYPE_WEAPON, i);
            CardCheckBox cb = new CardCheckBox(card, player.isCardInHand(card), player.getNotebook().isCardToggled(card));
            checkBoxes.add(cb);
            buttonGroup2.add(cb);
            table.add(cb);
            if ((i + 1) % 3 == 0) {
                table.row();
            }
        }

        table.row();
        table.add(new Label("", ClueMain.skin));
        table.row();

        TextButton close = new TextButton("OK", ClueMain.skin);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {

                    hide();

                    suggestion.add(roomCard);

                    for (CardCheckBox cb : checkBoxes) {
                        if (cb.isChecked() && !cb.isDisabled()) {
                            suggestion.add(cb.getCard());
                        }
                    }

                    showCards.setSuggestion(suggestion, screen.getYourPlayer());

                    SequenceAction seq = Actions.action(SequenceAction.class);
                    seq.addAction(Actions.delay(1f));
                    seq.addAction(Actions.run(new Runnable() {
                        public void run() {
                            showCards.showCards();
                        }
                    }));
                    screen.getStage().addAction(seq);
                }
                return false;
            }
        });
        table.add(close).size(120, 25);

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == SuggestionDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(SuggestionDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };
    }

    public void show(Stage stage) {

        clearActions();

        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();

        stage.addActor(this);
        //stage.setKeyboardFocus(playerSelection);
        stage.setScrollFocus(this);

        Gdx.input.setInputProcessor(stage);

        Action action = sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade));
        addAction(action);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        Action action = sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor());

        Stage stage = getStage();

        if (stage != null) {
            removeListener(focusListener);
        }

        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
    }

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    private class CardCheckBox extends CheckBox {

        Card card;

        public CardCheckBox(Card card, boolean inHand, boolean toggledInNotebook) {
            super(card.toString(), ClueMain.skin, inHand ? "card-in-hand" : toggledInNotebook ? "toggled-in-notebook" : "default");
            this.card = card;
            setDisabled(inHand);
        }

        public Card getCard() {
            return this.card;
        }
    }

}
