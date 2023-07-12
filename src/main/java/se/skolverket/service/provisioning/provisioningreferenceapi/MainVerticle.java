package se.skolverket.service.provisioning.provisioningreferenceapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.jackson.DatabindCodec;
import lombok.extern.slf4j.Slf4j;
import se.skolverket.service.provisioning.provisioningreferenceapi.dataingest.DataIngestGatewayVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.activities.ActivitiesServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.deletedentities.DeletedEntitiesVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.duties.DutiesServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.groups.GroupsServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.logging.LoggingServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.persons.PersonsServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.statistics.StatisticsServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.services.subscriptions.SubscriptionsServiceVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.ss12000api.SS12000ApiGatewayVerticle;
import se.skolverket.service.provisioning.provisioningreferenceapi.token.GuardianOfTheTokenVerticle;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");

    registerJavaTimeModule();

    ConfigStoreOptions env = new ConfigStoreOptions()
      .setType("env");

    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .setIncludeDefaultStores(true)
      .addStore(env);


    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

    retriever.getConfig().onSuccess(configuration -> {
      log.info("Config: {}", configuration.encodePrettily());
        List<Future<String>> deployments = new ArrayList<>();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(configuration);

        /* SERVICES */
        deployments.add(vertx.deployVerticle(GuardianOfTheTokenVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(PersonsServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(GroupsServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(ActivitiesServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(DutiesServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(DeletedEntitiesVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(SubscriptionsServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(LoggingServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(StatisticsServiceVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));

        /* GATEWAY */
        deployments.add(vertx.deployVerticle(DataIngestGatewayVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        deployments.add(vertx.deployVerticle(SS12000ApiGatewayVerticle.class.getName(), deploymentOptions)
          .onFailure(Throwable::printStackTrace));
        Future.all(deployments)
          .onSuccess(v -> {
            log.info("Provision reference api deployed.");
            startPromise.complete();
          })
          .onFailure(startPromise::fail);
      })
      .onFailure(throwable -> {
        log.error("Error loading config. ", throwable);
        startPromise.fail(throwable);
      });


  }


  private void registerJavaTimeModule() {
    ObjectMapper mapper = DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());
  }

}
