package domainapp.modules.simple.dom.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;

@Mixin(
        method = "act"
)
public class SimpleObject_impersonateUser {
    private final SimpleObject simpleObject;

    public SimpleObject_impersonateUser(final SimpleObject simpleObject) {
        this.simpleObject = simpleObject;
    }

    @Action(
            restrictTo = RestrictTo.PROTOTYPING,
            commandPersistence = CommandPersistence.NOT_PERSISTED
    )
    @MemberOrder(
            sequence = "90.1"
    )
    public Object act(
            //@Nullable
            final SimpleObject applicationUser,
            @ParameterLayout(describedAs = "If set, then the roles specified below are used.  Otherwise uses roles of the user.")
            final boolean useExplicitRolesBelow,
            @ParameterLayout(describedAs = "Only used if 'useExplicitRolesBelow' is set, otherwise is ignored.")
            @Nullable final List<SimpleObject> applicationRoleList) {

        return this.simpleObject;
    }

    public List<SimpleObject> choices0Act() {
        return this.simpleObjects.listAll();
    }
    public SimpleObject default0Act() {
        return simpleObject;
    }

    public boolean default1Act() {
        return false;
    }

    public List<SimpleObject> choices2Act() {
        return this.simpleObjects.listAll();
    }
    public List<SimpleObject> default2Act(final SimpleObject applicationUser) {
        final List<SimpleObject> simpleObjects = this.simpleObjects.listAll();
        if(applicationUser != null) {
            simpleObjects.remove(applicationUser);
        }
        return simpleObjects;
    }

    public boolean hideAct() {
        return false;
    }

    @Inject
    SimpleObjects simpleObjects;
}
