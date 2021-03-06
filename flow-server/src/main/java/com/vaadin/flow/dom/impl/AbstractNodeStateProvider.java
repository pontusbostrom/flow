/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.dom.impl;

import com.vaadin.flow.StateNode;
import com.vaadin.flow.dom.ChildElementConsumer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementStateProvider;
import com.vaadin.flow.dom.Node;
import com.vaadin.flow.dom.ShadowRoot;
import com.vaadin.flow.nodefeature.AttachExistingElementFeature;
import com.vaadin.flow.nodefeature.ElementChildrenList;
import com.vaadin.flow.nodefeature.NodeFeature;
import com.vaadin.flow.nodefeature.ShadowRootHost;

/**
 * Abstract implementation of the {@link ElementStateProvider} related to the
 * composition essence of the provider.
 * 
 * @author Vaadin Ltd
 *
 */
public abstract class AbstractNodeStateProvider
        implements ElementStateProvider {

    @Override
    public boolean supports(StateNode node) {
        for (Class<? extends NodeFeature> nsClass : getProviderFeatures()) {
            if (!node.hasFeature(nsClass)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the features supported by the provider.
     * 
     * @return features supported by the provider
     */
    protected abstract Class<? extends NodeFeature>[] getProviderFeatures();

    @Override
    public Node getParent(StateNode node) {
        StateNode parentNode = node.getParent();
        if (parentNode == null) {
            return null;
        }

        if (parentNode.hasFeature(ShadowRootHost.class)) {
            return ShadowRoot.get(parentNode);
        }

        return Element.get(node.getParent());
    }

    @Override
    public int getChildCount(StateNode node) {
        return getChildrenFeature(node).size();
    }

    @Override
    public Element getChild(StateNode node, int index) {
        assert index >= 0;
        assert index < getChildCount(node);

        return Element.get(getChildrenFeature(node).get(index));
    }

    @Override
    public void insertChild(StateNode node, int index, Element child) {
        assert index >= 0;
        assert index <= getChildCount(node); // == if adding as last

        getChildrenFeature(node).add(index, child.getNode());
    }

    @Override
    public void removeChild(StateNode node, int index) {
        assert index >= 0;
        assert index < getChildCount(node);

        getChildrenFeature(node).remove(index);
    }

    @Override
    public void removeAllChildren(StateNode node) {
        getChildrenFeature(node).clear();
    }

    @Override
    public void removeChild(StateNode node, Element child) {
        ElementChildrenList childrenFeature = getChildrenFeature(node);
        int pos = childrenFeature.indexOf(child.getNode());
        if (pos == -1) {
            throw new IllegalArgumentException("Not in the list");
        }
        childrenFeature.remove(pos);

    }

    @Override
    public void attachExistingElement(StateNode node, String tagName,
            Element previousSibling, ChildElementConsumer callback) {
        if (tagName == null) {
            throw new IllegalArgumentException(
                    "Tag name parameter cannot be null");
        }
        if (callback == null) {
            throw new IllegalArgumentException(
                    "Callback parameter cannot be null");
        }
        /*
         * create a node that should represent the client-side element. This
         * node won't be available anywhere and will be removed if there is no
         * appropriate element on the client-side. This node will be used after
         * client-side roundtrip for the appropriate element.
         */
        StateNode proposedNode = BasicElementStateProvider
                .createStateNode(tagName);

        node.runWhenAttached(ui -> {
            node.getFeature(AttachExistingElementFeature.class).register(
                    getNode(node), previousSibling, proposedNode, callback);
            ui.getPage().executeJavaScript(
                    "this.attachExistingElement($0, $1, $2, $3);",
                    getNode(node), previousSibling, tagName,
                    proposedNode.getId());
        });
    }

    /**
     * Gets the flyweight instance for the {@code node} supported by the
     * provider.
     * 
     * @see #supports(StateNode)
     * @param node
     *            the node to wrap into flyweight
     * @return the flyweight instance for the {@code node}
     */
    protected abstract Node<?> getNode(StateNode node);

    /**
     * Gets the children data feature for the given node and asserts it is
     * non-null.
     *
     * @param node
     *            the node
     * @return the children feature
     */
    private static ElementChildrenList getChildrenFeature(StateNode node) {
        return node.getFeature(ElementChildrenList.class);
    }

}
