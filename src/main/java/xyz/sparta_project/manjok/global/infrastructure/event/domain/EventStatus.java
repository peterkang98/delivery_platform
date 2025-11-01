package xyz.sparta_project.manjok.global.infrastructure.event.domain;

public enum EventStatus {
    PENDING {
        @Override
        public boolean canTransitionTo(EventStatus target) {
            return target == FAILED || target == SUCCESS;
        }

    },
    SUCCESS {
        @Override
        public boolean canTransitionTo(EventStatus target) {
            return false;
        }
    },
    FAILED {
        @Override
        public boolean canTransitionTo(EventStatus target) {
            return target == RETRYING;
        }
    },
    RETRYING {
        @Override
        public boolean canTransitionTo(EventStatus target) {
            return target == SUCCESS || target == FAILED;
        }
    },
    DEAD_LETTER {
        @Override
        public boolean canTransitionTo(EventStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(EventStatus target);
}
