package com.example.rrms.authsecurity.config;

import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.dto.response.TenantResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.record.RecordModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.registerModule(new RecordModule());
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true);

        modelMapper.createTypeMap(Tenant.class, TenantResponse.class).setConverter(context -> {
            Tenant tenant = context.getSource();
            if (tenant == null) return null;
            return new TenantResponse(
                    tenant.getId(),
                    tenant.getCode(),
                    tenant.getName(),
                    tenant.getStatus(),
                    tenant.getCreatedById(),
                    tenant.getCreatedAt()
            );
        });

        return modelMapper;
    }
}
