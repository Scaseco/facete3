package org.aksw.facete3.app.vaadin.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

public class ComponentBundleMaven
    implements ComponentBundle, HasValueChangeMode
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

            TextField groupIdTextField = new TextField();
            TextField artifactIdTextField = new TextField();
            TextField versionTextField = new TextField();

            FormItem groupIdFormItem = form.addFormItem(groupIdTextField, "Group ID");
            FormItem artifactIdFormItem = form.addFormItem(artifactIdTextField, "Artifact ID");
            FormItem versionFormItem = form.addFormItem(versionTextField, "Version ID");

            ComponentBundleMaven result = new ComponentBundleMaven(
                    form,
                    groupIdTextField, artifactIdTextField, versionTextField,
                    groupIdFormItem, artifactIdFormItem, versionFormItem);

            result.setValueChangeMode(ValueChangeMode.LAZY);
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

    @Override
    public ValueChangeMode getValueChangeMode() {
        return groupIdTextField.getValueChangeMode();
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        groupIdTextField.setValueChangeMode(valueChangeMode);
        artifactIdTextField.setValueChangeMode(valueChangeMode);
        versionTextField.setValueChangeMode(valueChangeMode);
    }
}
