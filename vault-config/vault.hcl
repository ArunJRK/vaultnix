storage "file" {
  path = "/vault/data"
}


api_addr = "http://0.0.0.0:8200"
cluster_addr = "http://0.0.0.0:8201"

ui = true

telemetry {
  prometheus_retention_time = "24h"
  disable_hostname          = true
  otlp_exporter {
    address = "uptrace:14317"
    protocol = "grpc"
  }
}


# New configuration for unauthenticated metrics access
listener "tcp" {
  tls_disable = 1
  address = "[::]:8200"
  telemetry {
    unauthenticated_metrics_access = true
  }
}

audit {
  type = "file"
  path = "stdout"
}