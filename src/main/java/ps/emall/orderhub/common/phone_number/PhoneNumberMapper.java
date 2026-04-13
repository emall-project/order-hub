package ps.emall.orderhub.common.phone_number;

public class PhoneNumberMapper {

    public static String toPhoneString(PhoneNumberDto dto) {
        if(dto == null || dto.getNumber() == null) {
            throw PhoneNumberExceptions.phoneRequired();
        }

        String prefix = dto.getPrefix();
        if(prefix == null || prefix.isBlank()) {
            prefix = "+972";
        }
        if (!prefix.equals("+972") && !prefix.equals("+970")) {
            throw PhoneNumberExceptions.invalidPrefix();
        }

        String number = dto.getNumber().trim();
        return prefix + "-" + number;
    }

    public static PhoneNumberDto fromPhoneString(String phoneNumber) {
        if(phoneNumber == null || !phoneNumber.contains("-")) return null;

        String[] parts = phoneNumber.split("-", 2);
        return PhoneNumberDto.builder()
                .prefix(parts[0])
                .number(parts[1])
                .build();
    }
}
