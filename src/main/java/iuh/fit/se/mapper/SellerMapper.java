package iuh.fit.se.mapper;

import iuh.fit.se.dto.request.SellerRegistrationRequest;
import iuh.fit.se.dto.response.SellerResponse;
import iuh.fit.se.entity.Seller;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SellerMapper {

    Seller toSeller(SellerRegistrationRequest request);
    @Mapping(source = "id", target = "Id")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "identificationLinks", target = "identificationLink")
    SellerResponse toSellerResponse(Seller seller);
}
