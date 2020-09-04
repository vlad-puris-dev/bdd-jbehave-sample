package com.vvp.sample.unit;

import java.util.Arrays;
import java.util.List;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.springframework.test.context.ActiveProfiles;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.StoryReporterBuilder;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.STATS;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;

@ActiveProfiles("test")
public class UnitTestRunner extends JUnitStories {
    /**
     * Setup test runner configuration.
     */
    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
          .useStoryLoader(new LoadFromClasspath(this.getClass()))
          .useStoryReporterBuilder(new StoryReporterBuilder()
            .withCodeLocation(codeLocationFromClass(this.getClass()))
            .withFormats(STATS, CONSOLE, TXT, HTML, XML)
            .withRelativeDirectory("output/testing-reports/unit"));
    }

    /**
     * Setup story steps definitions.
     */
    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new UnitStepDefinitions());
    }

    /**
     * Provide stories path.
     */
    @Override
    protected List<String> storyPaths() {
        return Arrays.asList("stories/account.story");
    }
}
