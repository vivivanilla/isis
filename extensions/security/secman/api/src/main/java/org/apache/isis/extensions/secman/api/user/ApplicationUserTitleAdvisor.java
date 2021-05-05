package org.apache.isis.extensions.secman.api.user;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermission;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE)
public
class ApplicationUserTitleAdvisor {

    @EventListener(ApplicationUser.TitleUiEvent.class)
    public void on(ApplicationUser.TitleUiEvent ev) {
        val user = ev.getSource();

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

}
