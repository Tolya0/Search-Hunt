module org.kurilin.recruitment.shared {
    requires org.slf4j;
    requires com.google.gson;
    requires jakarta.persistence;
    requires static lombok;
    requires org.hibernate.orm.core;

    opens org.kurilin.recruitment.shared.entity to org.hibernate.orm.core;
    opens org.kurilin.recruitment.shared.network.dto to com.google.gson;
    opens org.kurilin.recruitment.shared.network to com.google.gson;

    exports org.kurilin.recruitment.shared.entity;
    exports org.kurilin.recruitment.shared.enums;
    exports org.kurilin.recruitment.shared.exception;
    exports org.kurilin.recruitment.shared.network;
    exports org.kurilin.recruitment.shared.network.dto;
    exports org.kurilin.recruitment.shared.util;
}