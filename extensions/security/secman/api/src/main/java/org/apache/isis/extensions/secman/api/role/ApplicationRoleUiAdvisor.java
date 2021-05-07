package org.apache.isis.extensions.secman.api.role;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE)
public
class ApplicationRoleUiAdvisor {

    @EventListener(ApplicationRole.TitleUiEvent.class)
    public void on(ApplicationRole.TitleUiEvent ev) {
        val role = ev.getSource();
        if(role == null) {
            return;
        }
        ev.setTitle(role.getName());
    }

}
