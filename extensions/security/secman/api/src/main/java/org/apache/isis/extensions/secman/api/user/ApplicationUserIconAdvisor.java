package org.apache.isis.extensions.secman.api.user;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE)
public
class ApplicationUserIconAdvisor {

    @EventListener(ApplicationUser.IconUiEvent.class)
    public void on(ApplicationUser.IconUiEvent ev) {
        val user = ev.getSource();
        ev.setIconName(
                user.getStatus().isEnabled()
                        ? "enabled"
                        : "disabled");
    }

}
