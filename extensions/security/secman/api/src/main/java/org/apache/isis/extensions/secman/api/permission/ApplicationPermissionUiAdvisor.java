package org.apache.isis.extensions.secman.api.permission;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE)
public
class ApplicationPermissionUiAdvisor {

    @EventListener(ApplicationPermission.TitleUiEvent.class)
    public void on(ApplicationPermission.TitleUiEvent ev) {
        val permission = ev.getSource();
        if(permission == null) {
            return;
        }
        val buf = new StringBuilder();
        buf.append(permission.getRole().getName()).append(":")  // admin:
                .append(" ").append(permission.getRule().toString()) // Allow|Veto
                .append(" ").append(permission.getMode().toString()) // Viewing|Changing
                .append(" of ");

        permission.asFeatureId()
                .ifPresent(featureId -> {

                    switch (featureId.getSort()) {
                        case NAMESPACE:
                            buf.append(permission.getFeatureFqn());              // com.mycompany
                            break;
                        case TYPE:
                            // abbreviate if required because otherwise title overflows on action prompt.
                            if (permission.getFeatureFqn().length() < 30) {
                                buf.append(permission.getFeatureFqn());          // com.mycompany.Bar
                            } else {
                                buf.append(featureId.getTypeSimpleName()); // Bar
                            }
                            break;
                        case MEMBER:
                            buf.append(featureId.getTypeSimpleName())
                                    .append("#")
                                    .append(featureId.getMemberName());   // com.mycompany.Bar#foo
                            break;
                    }

                });

        ev.setTitle(buf.toString());
    }

}
