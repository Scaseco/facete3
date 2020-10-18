package org.aksw.facete3.app.vaadin.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.DataProvider;

public class DataProviderUtils {
    public static <T, F> DataProvider<T, F> wrapWithErrorHandler(DataProvider<T, F> dataProvider) {
        DataProvider<T, F> result = new DataProviderWrapperWithCustomErrorHandler<>(
                dataProvider,
                th -> Notification.show(ExceptionUtils.getRootCauseMessage(th)));
        return result;
    }
}
