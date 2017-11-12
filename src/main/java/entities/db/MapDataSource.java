package entities.db;

import entities.MapEdge;
import entities.MapNode;

import java.util.ArrayList;

public interface MapDataSource {
    /**
     * Gets a node from the data source
     * @param id
     * @return
     */
    default MapNode getNode(String id) {
        return null;
    }

    /**
     * Adds a node to the data source
     * @param node
     */
    default void addNode(MapNode node) {

    }

    /**
     * Removes a node from the data source
     * @param id
     */
    default void removeNode(String id) {

    }

    /**
     * Adds an edge
     * @param edge
     */
    default void addEdge(MapEdge edge) {

    }

    /**
     * Gets an array of all the node id strings
     * @return
     */
    default ArrayList<String> getNodeIds() {
        return null;
    }

    /**
     * Returns an array of all the edge id strings
     * @return
     */
    default ArrayList<String> getEdgeIds() {
        return null;
    }

    /**
     * Gets a specified edge by id
     * @param id
     * @return
     */
    default MapEdge getEdge(String id) { return null; }

    default void close() {

    }
}