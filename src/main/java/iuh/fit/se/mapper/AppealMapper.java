package iuh.fit.se.mapper;

import iuh.fit.se.dto.response.AppealResponse;
import iuh.fit.se.entity.Appeal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppealMapper {

    @Mapping(target = "sellerName", source = "seller.shopName")
    @Mapping(target = "sellerEmail", source = "seller.email")
    @Mapping(target = "sellerId", source = "seller.id")
    AppealResponse toResponse(Appeal appeal);
}