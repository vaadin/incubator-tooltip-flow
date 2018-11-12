package com.vaadin.flow.component.incubator;

/*
 * #%L
 * Vaadin IncubatorTooltip for Vaadin 10
 * %%
 * Copyright (C) 2017 - 2018 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */


import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.internal.nodefeature.ElementData;
import com.vaadin.flow.shared.Registration;

/**
 * Server-side component for the <code>incubator-tooltip</code> element.
 * Default tooltip's position and alignment are top and center respectively.
 *
 * @author Vaadin Ltd
 */
@Tag("incubator-tooltip")
@HtmlImport("bower_components/incubator-tooltip/src/incubator-tooltip.html")
public class Tooltip extends Component implements HasComponents, HasStyle {

    /**
     * Click event on the component.
     */
    @DomEvent("click")
    public static class ClickEvent extends ComponentEvent<Tooltip> {

        public ClickEvent(Tooltip source, boolean fromClient) {
            super(source, fromClient);
        }
    }

    private final String POSITION_PROPERTY = "position";
    private final String ALIGNMENT_PROPERTY = "align";
    private final String HIDDEN_MSG_PROPERTY = "hidden";
    private final String MANUAL_PROPERTY = "manual";

    private Registration detachedRegistration;

    /**
     * Default constructor.
     */
    public Tooltip() {
        getElement().getStyle().set("margin", "0px");
    }

    /**
     * Creates the tooltip attaching it to the component.
     * <p>
     * The tooltip is automatically appended to the component's parent.
     *
     * @param component the tooltip is attached to this component
     */
    public Tooltip(Component component) {
        this();
        attachToComponent(component);
    }

    /**
     * Creates the tooltip attaching it to the component and sets its position.
     * <p>
     * The tooltip is automatically appended to the component's parent.
     *
     * @param component the tooltip is attached to this component
     * @param position  The position of the tooltip {@link TooltipPosition}
     */
    public Tooltip(Component component, TooltipPosition position) {
        this(component);
        setPosition(position);
    }

    /**
     * Creates the tooltip attaching it to the component. It also sets its position
     * and its alignment.
     * <p>
     * The tooltip is automatically appended to the component's parent.
     *
     * @param component the tooltip is attached to this component
     * @param position  The position of the tooltip {@link TooltipPosition}
     * @param alignment The alignment of the tooltip {@link TooltipAlignment}
     */
    public Tooltip(Component component, TooltipPosition position, TooltipAlignment alignment) {
        this(component, position);
        setAlignment(alignment);
    }

    /**
     * Assigns the tooltip to a specific component.
     * <p>
     * The tooltip is automatically appended to the component's parent.
     * <p>
     * The tooltip is removed from the parent after the component that the tooltip is
     * attached is detached.
     *
     * @param component the tooltip is attached to this component
     */
    public void attachToComponent(Component component){
        attachToComponent(component,true);
    }

    /**
     * Assigns the tooltip to a specific component.
     * <p>
     * The tooltip is removed from the parent after the component that the tooltip is
     * attached is detached.
     *
     * @param component the tooltip is attached to this component
     * @param appended <code>true</code> the tooltip is automatically appended
     *                 to the component's father.<code>false</code>,
     *                 it is not appended. It should be added to a layout manually.
     */
    public void attachToComponent(Component component, boolean appended) {
        Objects.requireNonNull(component);

        getElement().getNode().runWhenAttached(ui -> {
            ui.getPage().executeJavaScript("$0.targetElement = $1;",
                    getElement(), component.getElement()
            );
        });

        if ( appended ){
            component.getElement().getNode().runWhenAttached(ui -> {
                component.getElement().getParentNode().appendChild(getElement());
            });
        }

        if ( detachedRegistration != null ){
            detachedRegistration.remove();
        }

        detachedRegistration = component.addDetachListener(event -> {
            this.getElement().removeFromParent();
        });
    }

    /**
     * Opens the content of the tooltip.
     */
    public void open() {
        getElement().setProperty(HIDDEN_MSG_PROPERTY, false);
    }

    /**
     * Hides the content of the tooltip.
     */
    public void close() {
        getElement().setProperty(HIDDEN_MSG_PROPERTY, true);
    }

    /**
     * Handle component enable state when the enabled state changes.
     * <p>
     * By default this sets or removes the 'disabled' attribute from the
     * element. This can be overridden to have custom handling.
     *
     * @param enabled the new enabled state of the component
     */
    @Override
    public void onEnabledStateChanged(boolean enabled) {
        // If the node has feature ElementData, then we know that the state
        // provider accepts attributes
        if (getElement().getNode().hasFeature(ElementData.class)) {
            getElement().callFunction("hide"); // needed to close tooltip
            getElement().setAttribute(MANUAL_PROPERTY, !enabled);
        }
    }

    /**
     * Checks the manual mode of the tooltip.
     *
     * @return manualMode <code>true</code> the tooltip is controlled programmatically
     *                    <code>false</code>, it is controlled automatically
     */
    public boolean isManualMode() {
        return getElement().getProperty(MANUAL_PROPERTY, false);
    }

    /**
     * Sets the manual mode of the tooltip.
     * <p>
     * manualMode requires to open or close the tooltip manually.
     *
     * @param manualMode <code>true</code> the tooltip is controlled programmatically
     *                   <code>false</code>, it is controlled automatically
     */
    public void setManualMode(boolean manualMode) {
        getElement().setProperty(MANUAL_PROPERTY, manualMode);
    }

    /**
     * Adds a listener for {@code ClickEvent}.
     *
     * @param listener the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addClickListener(ComponentEventListener<ClickEvent> listener) {
        return addListener(ClickEvent.class, listener);
    }

    /**
     * Sets the position of the tooltip.
     *
     * @param position The position of the tooltip {@link TooltipPosition}
     */
    public void setPosition(TooltipPosition position) {
        setPosition(position.getPositionText());
    }

    /**
     * Gets the position of the tooltip.
     *
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return position The position of the tooltip {@link TooltipPosition}
     **/
    public TooltipPosition getPosition() {
        return TooltipPosition.getPosition(getPositionText());
    }

    /**
     * Sets the alignment of the tooltip.
     *
     * @param alignment The alignment of the tooltip {@link TooltipAlignment}
     */
    public void setAlignment(TooltipAlignment alignment) {
        setAlignment(alignment.getAlignmentText());
    }

    /**
     * Gets the alignment of the tooltip.
     *
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return alignment The alignment of the tooltip {@link TooltipAlignment}
     **/
    public TooltipAlignment getAlignment() {
        return TooltipAlignment.getAlignment(getAlignmentText());
    }

    /**
     * Sets the position of the tooltip.
     *
     * @param position "top","right","left" or "bottom"
     */
    private void setPosition(String position) {
        getElement().setProperty(POSITION_PROPERTY, position);
    }

    /**
     * Gets the position of the tooltip.
     *
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return position "top","right","left" or "bottom"
     */
    private String getPositionText() {
        return getElement().getProperty(POSITION_PROPERTY);
    }

    /**
     * Sets the alignment of the tooltip.
     *
     * @param alignment alignment "top","right","left","bottom" or "center"
     */
    private void setAlignment(String alignment) {
        getElement().setProperty(ALIGNMENT_PROPERTY, alignment);
    }

    /**
     * Gets the alignment of the tooltip.
     *
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return alignment "top","right","left","bottom" or center
     */
    private String getAlignmentText() {
        return getElement().getProperty(ALIGNMENT_PROPERTY);
    }
}