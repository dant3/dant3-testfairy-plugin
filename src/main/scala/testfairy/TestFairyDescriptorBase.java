package testfairy;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

public abstract class TestFairyDescriptorBase extends BuildStepDescriptor<Publisher> {
    public TestFairyDescriptorBase() {
        super(TestFairyRecorder.class);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }
}
