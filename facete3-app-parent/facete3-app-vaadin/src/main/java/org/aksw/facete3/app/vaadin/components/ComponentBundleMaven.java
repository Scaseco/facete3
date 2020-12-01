package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.textfield.TextField;

public class ComponentBundleMaven
    implements ComponentBundle
{
    protected Component targetComponent;

    protected TextField groupIdTextField;
    protected TextField artifactIdTextField;
    protected TextField versionTextField;

    protected FormItem groupIdFormItem;
    protected FormItem artifactIdFormItem;
    protected FormItem versionFormItem;

    public ComponentBundleMaven(
            Component targetComponent,
            TextField groupIdTextField, TextField artifactIdTextField, TextField versionTextField,
            FormItem groupIdFormItem, FormItem artifactIdFormItem, FormItem versionFormItem) {
        super();
        this.targetComponent = targetComponent;
        this.groupIdTextField = groupIdTextField;
        this.artifactIdTextField = artifactIdTextField;
        this.versionTextField = versionTextField;
        this.groupIdFormItem = groupIdFormItem;
        this.artifactIdFormItem = artifactIdFormItem;
        this.versionFormItem = versionFormItem;
    }

    public static class Installer
        implements ComponentInstaller
    {
        @Override
        public ComponentBundleMaven install(Component target) {
            FormLayout form = (FormLayout)target;

            TextField groupIdField = new TextField();
            TextField artifactIdField = new TextField();
            TextField versionField = new TextField();

            FormItem formItemX = form.addFormItem(groupIdField, "Group ID");
            FormItem formItemY = form.addFormItem(artifactIdField, "Artifact ID");
            FormItem formItemZ = form.addFormItem(versionField, "Version ID");

            ComponentBundleMaven result = new ComponentBundleMaven(
                    form,
                    groupIdField, artifactIdField, versionField,
                    formItemX, formItemY, formItemZ);

            return result;
        }
    }

    @Override
    public void close() {
    }

    public Component getTargetComponent() {
        return targetComponent;
    }

    public TextField getGroupIdTextField() {
        return groupIdTextField;
    }

    public TextField getArtifactIdTextField() {
        return artifactIdTextField;
    }

    public TextField getVersionTextField() {
        return versionTextField;
    }

    public FormItem getGroupIdFormItem() {
        return groupIdFormItem;
    }

    public FormItem getArtifactIdFormItem() {
        return artifactIdFormItem;
    }

    public FormItem getVersionFormItem() {
        return versionFormItem;
    }
}
