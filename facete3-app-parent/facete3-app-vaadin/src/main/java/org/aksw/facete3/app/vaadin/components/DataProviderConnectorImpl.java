package org.aksw.facete3.app.vaadin.components;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.aksw.jena_sparql_api.vaadin.data.provider.DataProviderConnector;
import org.aksw.jena_sparql_api.vaadin.util.GridLike;
import org.aksw.jena_sparql_api.vaadin.util.GridWrapperBase;
import org.aksw.vaadin.common.provider.util.DataProviderUtils;
import org.aksw.vaadin.common.provider.util.DataProviderWithTaskControl;
import org.aksw.vaadin.common.provider.util.TaskControlRegistry;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderConnectorImpl
    implements DataProviderConnector
{
    protected TaskControlRegistry taskControlRegistry;
    protected ExecutorService executorService;

    public DataProviderConnectorImpl(TaskControlRegistry taskControlRegistry, ExecutorService executorService) {
        super();
        this.taskControlRegistry = Objects.requireNonNull(taskControlRegistry);
        this.executorService = Objects.requireNonNull(executorService);
    }

    public TaskControlRegistry getTaskControlRegistry() {
        return taskControlRegistry;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void connectRaw(Object component, DataProvider<?, ?> dataProvider, String taskName) {
        if (component == null) {
            throw new NullPointerException();
        } else if (component instanceof Grid) {
            Grid grid = (Grid)component;
            connectGrid(grid, dataProvider, taskName);
        } else if (component instanceof GridLike) {
            GridLike gridLike = (GridLike)component;
            connect(gridLike, dataProvider, taskName);
        } else {
            throw new IllegalArgumentException("Don't know how to handle component " + component);
        }
    }

    @Override
    public <T> void connectGrid(Grid<T> grid, DataProvider<T, ?> dataProvider, String taskName) {
        connect(new GridWrapperBase<>(grid), dataProvider, taskName);
    }

    @Override
    public <T> void connect(GridLike<T> grid, DataProvider<T, ?> dataProvider, String taskName) {
        // TaskControlRegistry taskControlRegistry = getTaskControlRegistry();
        // ExecutorService executorService = getExecutorService();

        grid.setDataProvider(DataProviderWithTaskControl.wrap(DataProviderUtils.wrapWithErrorHandler(dataProvider), taskControlRegistry, grid, taskName));
        grid.getDataCommunicator().enablePushUpdates(executorService);
    }
}
