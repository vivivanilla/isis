package org.apache.isis.extensions.secman.api.user;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE)
public
class ApplicationUserUiAdvisor {

    @EventListener(ApplicationUser.TitleUiEvent.class)
    public void on(ApplicationUser.TitleUiEvent ev) {
        val user = ev.getSource();
        if(user == null) {
            return;
        }

        val buf = new StringBuilder();
        if(user.getFamilyName() != null) {
            buf.append(
                    user.getKnownAs() != null
                            ? user.getKnownAs()
                            : user.getGivenName())
                    .append(' ')
                    .append(user.getFamilyName())
                    .append(" (").append(user.getUsername()).append(')');
        } else {
            buf.append(user.getUsername());
        }
        ev.setTitle(buf.toString());
    }

    @EventListener(ApplicationUser.IconUiEvent.class)
    public void on(ApplicationUser.IconUiEvent ev) {
        val user = ev.getSource();
        if(user == null) {
            return;
        }
        ev.setIconName(
                user.getStatus().isEnabled()
                        ? "enabled"
                        : "disabled");
    }

}
