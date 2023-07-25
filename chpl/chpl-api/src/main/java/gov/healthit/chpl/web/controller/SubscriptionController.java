package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.subscription.SubscriptionManager;
import gov.healthit.chpl.subscription.domain.ChplItemSubscriptionGroup;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRequest;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionRequest;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "subscriptions", description = "Manage public user subscriptions.")
@RestController
public class SubscriptionController {
    private SubscriptionManager subscriptionManager;
    private FF4j ff4j;

    @Autowired
    public SubscriptionController(SubscriptionManager subscriptionManager,
            FF4j ff4j) {
        this.subscriptionManager = subscriptionManager;
        this.ff4j = ff4j;
    }

    @Operation(summary = "Get available subscriber roles.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/roles", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SubscriberRole> getSubscriptionRoles() {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        return subscriptionManager.getAllRoles();
    }

    @Operation(summary = "Get available types of things that may be subscribed to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions/types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<SubscriptionObjectType> getSubscribedObjectTypes() {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        return subscriptionManager.getAllSubscriptionObjectTypes();
    }

    @Operation(summary = "Get information about a subscriber.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId:^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Subscriber getSubscriber(@PathVariable String subscriberId)
        throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        Subscriber subscriber = subscriptionManager.getSubscriber(UUID.fromString(subscriberId));
        if (subscriber == null) {
            throw new EntityRetrievalException("Subscriber was not found.");
        }
        return subscriber;
    }

    @Operation(summary = "Gets all the subscriptions for a subscriber grouped by each item in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscribers/{subscriberId}/subscriptions", method = RequestMethod.GET, produces = "application/json; charset=utf-8",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<? extends ChplItemSubscriptionGroup> getSubscriptionsForSubscriber(@PathVariable String subscriberId)
        throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        Subscriber subscriber = subscriptionManager.getSubscriber(UUID.fromString(subscriberId));
        if (subscriber == null) {
            throw new EntityRetrievalException("Subscriber was not found.");
        }
        return subscriptionManager.getGroupedSubscriptions(UUID.fromString(subscriberId));
    }

    @Operation(summary = "Subscribe to periodic notifications about changes to a specific item in the CHPL. "
            + "A new subscriber will be created and will need confirm their email address "
            + "if there is not currently a subscriber with this email address.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions", method = RequestMethod.POST, produces = "application/json; charset=utf-8",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Subscriber subscribe(@RequestBody(required = true) SubscriptionRequest subscriptionRequest)
        throws ValidationException {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        return subscriptionManager.subscribe(subscriptionRequest);
    }

    @Operation(summary = "Confirm a subscriber's email address is valid. Once confirmed, the subscriber "
            + "will start receiving notifications at the specified email address.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions/confirm-subscriber", method = RequestMethod.PUT, produces = "application/json; charset=utf-8",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Subscriber confirmSubscriber(@RequestBody(required = true) SubscriberRequest request)
        throws ValidationException {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        return subscriptionManager.confirm(UUID.fromString(request.getSubscriberId()));
    }

    @Operation(summary = "Unsubscribe from all notifications associated with a subscriber.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/subscriptions/unsubscribe-all", method = RequestMethod.PUT, produces = "application/json; charset=utf-8",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void unsubscribeAll(@RequestBody(required = true) SubscriberRequest request)
        throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        subscriptionManager.unsubscribeAll(UUID.fromString(request.getSubscriberId()));
    }
}
