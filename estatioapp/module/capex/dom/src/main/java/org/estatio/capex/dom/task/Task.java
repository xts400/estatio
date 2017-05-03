package org.estatio.capex.dom.task;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.user.UserService;

import org.estatio.capex.dom.state.StateTransitionType;
import org.estatio.dom.roles.EstatioRole;

import lombok.Getter;
import lombok.Setter;

@PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "dbo"
)
@javax.jdo.annotations.Discriminator(
        strategy = DiscriminatorStrategy.VALUE_MAP,
        column = "discriminator",
        value = "org.estatio.capex.dom.task.Task"
)
@DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE,
        column = "id")
@Version(
        strategy = VersionStrategy.VERSION_NUMBER,
        column = "version")
@Queries({
        @Query(
                name = "find", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.estatio.capex.dom.task.Task ")
})
@DomainObject(objectType = "task.Task")
public class Task implements Comparable<Task> {

    @Mixin(method="act")
    public static class execute {

        private final Task task;
        public execute(final Task task ) {
            this.task = task;
        }

        @Action(semantics = SemanticsOf.IDEMPOTENT)
        @ActionLayout(contributed= Contributed.AS_ACTION)
        public Object act() {
            final Object obj = domainObjectFor(task);
            StateTransitionType.Util.apply(obj, transitionTypeFor(task), serviceRegistry);
            return obj;
        }
        public String disableAct() {
            return task.isCompleted()
                    ? String.format("Already completed (on %s by %s)", task.getCompletedBy(), task.getCompletedBy())
                    : null;
        }

        private Object domainObjectFor(Task task) {
            // TODO
            return null;
        }

        private StateTransitionType transitionTypeFor(Task task) {
            // TODO
            return null;
        }

        @Inject
        private ServiceRegistry2 serviceRegistry;

    }


    @Programmatic
    public void completed() {

        final LocalDateTime completedOn = clockService.nowAsLocalDateTime();
        final String completedBy = userService.getUser().getName();

        setCompleted(true);
        setCompletedOn(completedOn);
        setCompletedBy(completedBy);

    }

    @Column(allowsNull = "true")
    @Getter @Setter
    private String description;

    @Getter @Setter
    @Column(allowsNull = "false")
    private LocalDateTime createdOn;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDateTime startedOn;

    @Column(allowsNull = "false")
    @Getter @Setter
    private boolean completed;

    @Getter @Setter
    @Column(allowsNull = "true")
    private LocalDateTime completedOn;

    @Getter @Setter
    @Column(allowsNull = "true")
    private String completedBy;

    @Getter @Setter
    @Column(allowsNull = "false")
    private EstatioRole assignedTo;

    @Override
    public int compareTo(final Task other) {
        return org.apache.isis.applib.util.ObjectContracts.compare(this, other, "createdOn");
    }

    @Inject
    UserService userService;

    @Inject
    ClockService clockService;



}
