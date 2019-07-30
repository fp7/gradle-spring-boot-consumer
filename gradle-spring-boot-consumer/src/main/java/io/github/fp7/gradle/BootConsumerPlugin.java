package io.github.fp7.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

public class BootConsumerPlugin implements Plugin<Project> {

  @Override
  public void apply(Project target) {
    target
        .getPlugins()
        .withType(
            JavaPlugin.class,
            fp -> {
              Configuration bootCfg = target.getConfigurations().maybeCreate("boot");

              TaskProvider<Copy> bootDeps =
                  target
                      .getTasks()
                      .register(
                          "bootDependencies",
                          Copy.class,
                          task -> {
                            task.getInputs().files(bootCfg);
                            Provider<Directory> jars =
                                target.getLayout().getBuildDirectory().dir("jars");
                            task.getOutputs().dir(jars);

                            task.into(jars);

                            ResolvedConfiguration resolvedConfiguration =
                                bootCfg.getResolvedConfiguration();

                            resolvedConfiguration
                                .getFirstLevelModuleDependencies()
                                .forEach(
                                    dep ->
                                        resolvedConfiguration
                                            .getFiles()
                                            .forEach(
                                                f -> {
                                                  task.from(
                                                      target.zipTree(f),
                                                      cp -> {
                                                        String targetPath =
                                                            String.format(
                                                                "%s/%s/%s",
                                                                dep.getModuleGroup(),
                                                                dep.getModuleName(),
                                                                dep.getModuleVersion());

                                                        cp.into(targetPath)
                                                            .include("BOOT-INF/classes/");

                                                        SourceSet mainSourceSet =
                                                            target
                                                                .getExtensions()
                                                                .getByType(SourceSetContainer.class)
                                                                .getByName(
                                                                    SourceSet.MAIN_SOURCE_SET_NAME);

                                                        target
                                                            .getDependencies()
                                                            .add(
                                                                JavaPlugin
                                                                    .IMPLEMENTATION_CONFIGURATION_NAME,
                                                                target.files(
                                                                    String.format(
                                                                        "%s/%s/%s",
                                                                        "build/jars",
                                                                        targetPath,
                                                                        "BOOT-INF/classes/")));

                                                        ;
                                                      });
                                                }));
                          });

              target.getTasks().getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(bootDeps);
            });
  }
}
