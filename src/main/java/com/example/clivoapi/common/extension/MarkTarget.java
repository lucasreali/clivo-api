package com.example.clivoapi.common.extension;

public enum MarkTarget {

    REGION {
        @Override
        public boolean accepts(RegionMarking marking) {
            return marking.coversTheWholeRegion();
        }

        @Override
        public String demand() {
            return "the region as a whole, with no part";
        }
    },
    PART {
        @Override
        public boolean accepts(RegionMarking marking) {
            return !marking.coversTheWholeRegion();
        }

        @Override
        public String demand() {
            return "at least one part of the region";
        }
    },
    ANY {
        @Override
        public boolean accepts(RegionMarking marking) {
            return true;
        }

        @Override
        public String demand() {
            return "the region as a whole or any of its parts";
        }
    };

    public abstract boolean accepts(RegionMarking marking);

    public abstract String demand();
}
