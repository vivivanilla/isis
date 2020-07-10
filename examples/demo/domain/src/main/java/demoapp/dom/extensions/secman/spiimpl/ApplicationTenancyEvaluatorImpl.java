package demoapp.dom.extensions.secman.spiimpl;

import org.springframework.stereotype.Service;

import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyEvaluator;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

import demoapp.dom.types.primitive.bytes.holder.PrimitiveByteHolder;

@Service
public class ApplicationTenancyEvaluatorImpl implements ApplicationTenancyEvaluator {

    @Override
    public boolean handles(Class<?> cls) {
        return PrimitiveByteHolder.class.isAssignableFrom(cls);
    }

    @Override
    public String hides(Object domainObject, ApplicationUser applicationUser) {
        PrimitiveByteHolder byteHolder = (PrimitiveByteHolder) domainObject;
        return byteHolder.getReadOnlyProperty() == 3 ? "hidden" : null;
    }

    @Override
    public String disables(Object domainObject, ApplicationUser applicationUser) {
        return null;
    }
}
